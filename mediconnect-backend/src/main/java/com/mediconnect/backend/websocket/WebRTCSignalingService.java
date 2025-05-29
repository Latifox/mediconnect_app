package com.mediconnect.backend.websocket;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.repository.AppointmentRepository;
import com.mediconnect.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebRTCSignalingService {

    private static final Logger logger = LoggerFactory.getLogger(WebRTCSignalingService.class);

    // Stores user ID to socket client mapping
    private final Map<Long, SocketIOClient> userSocketMap = new ConcurrentHashMap<>();
    
    // Stores active call rooms by appointment ID
    private final Map<Long, CallRoom> activeCallRooms = new ConcurrentHashMap<>();

    private final SocketIOServer socketIOServer;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public WebRTCSignalingService(SocketIOServer socketIOServer, 
                                 AppointmentRepository appointmentRepository,
                                 UserRepository userRepository) {
        this.socketIOServer = socketIOServer;
        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        
        // Register event listeners
        this.socketIOServer.addConnectListener(this::onConnectHandler);
        this.socketIOServer.addDisconnectListener(this::onDisconnectHandler);
        
        // Register WebRTC signaling events
        this.socketIOServer.addEventListener("join-call", JoinCallRequest.class, this::handleJoinCall);
        this.socketIOServer.addEventListener("leave-call", LeaveCallRequest.class, this::handleLeaveCall);
        this.socketIOServer.addEventListener("webrtc-signal", WebRTCSignalData.class, this::handleWebRTCSignal);
        this.socketIOServer.addEventListener("toggle-media", ToggleMediaRequest.class, this::handleToggleMedia);
    }

    @OnConnect
    public void onConnectHandler(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        if (userId != null && !userId.isEmpty()) {
            try {
                Long userIdLong = Long.parseLong(userId);
                userSocketMap.put(userIdLong, client);
                logger.info("Client connected: {}, userId: {}", client.getSessionId(), userId);
            } catch (NumberFormatException e) {
                logger.error("Invalid user ID format: {}", userId);
                client.disconnect();
            }
        } else {
            logger.error("User ID not provided in connection");
            client.disconnect();
        }
    }

    @OnDisconnect
    public void onDisconnectHandler(SocketIOClient client) {
        String userId = client.getHandshakeData().getSingleUrlParam("userId");
        if (userId != null && !userId.isEmpty()) {
            try {
                Long userIdLong = Long.parseLong(userId);
                userSocketMap.remove(userIdLong);
                
                // Check if user was in a call and handle call cleanup
                for (Map.Entry<Long, CallRoom> entry : activeCallRooms.entrySet()) {
                    CallRoom room = entry.getValue();
                    if (room.hasParticipant(userIdLong)) {
                        // Notify other participant that this user has left
                        Long otherUserId = room.getOtherParticipantId(userIdLong);
                        if (otherUserId != null) {
                            SocketIOClient otherClient = userSocketMap.get(otherUserId);
                            if (otherClient != null) {
                                otherClient.sendEvent("user-disconnected", userIdLong);
                            }
                        }
                        
                        // Remove user from the room
                        room.removeParticipant(userIdLong);
                        
                        // If room is empty, remove it
                        if (room.isEmpty()) {
                            activeCallRooms.remove(entry.getKey());
                        }
                    }
                }
                
                logger.info("Client disconnected: {}, userId: {}", client.getSessionId(), userId);
            } catch (NumberFormatException e) {
                logger.error("Invalid user ID format on disconnect: {}", userId);
            }
        }
    }

    public void handleJoinCall(SocketIOClient client, JoinCallRequest request, AckRequest ackRequest) {
        Long userId = request.getUserId();
        Long appointmentId = request.getAppointmentId();
        
        logger.info("User {} is joining call for appointment {}", userId, appointmentId);
        
        // Validate the appointment
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        if (appointmentOpt.isEmpty()) {
            sendErrorToClient(client, "Appointment not found");
            return;
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // Verify that the user is either the doctor or patient for this appointment
        if (!userId.equals(appointment.getDoctor().getId()) && !userId.equals(appointment.getPatient().getId())) {
            sendErrorToClient(client, "Unauthorized to join this call");
            return;
        }
        
        // Get or create call room
        CallRoom callRoom = activeCallRooms.computeIfAbsent(appointmentId, id -> new CallRoom(appointmentId));
        
        // Add user to the room
        callRoom.addParticipant(userId);
        
        // If this is the first participant, just acknowledge join
        if (callRoom.getParticipantCount() == 1) {
            client.sendEvent("call-joined", new CallJoinedResponse(appointmentId, false));
            return;
        }
        
        // Get the other participant's ID (doctor or patient)
        Long otherUserId = userId.equals(appointment.getDoctor().getId()) 
            ? appointment.getPatient().getId() 
            : appointment.getDoctor().getId();
        
        // Notify the other participant that someone has joined
        SocketIOClient otherClient = userSocketMap.get(otherUserId);
        if (otherClient != null) {
            otherClient.sendEvent("user-joined", new UserJoinedResponse(userId, appointmentId));
        }
        
        // Acknowledge successful join with peerExists=true since there's another peer
        client.sendEvent("call-joined", new CallJoinedResponse(appointmentId, true));
    }

    public void handleLeaveCall(SocketIOClient client, LeaveCallRequest request, AckRequest ackRequest) {
        Long userId = request.getUserId();
        Long appointmentId = request.getAppointmentId();
        
        logger.info("User {} is leaving call for appointment {}", userId, appointmentId);
        
        CallRoom callRoom = activeCallRooms.get(appointmentId);
        if (callRoom != null) {
            // Get the other participant before removing this user
            Long otherUserId = callRoom.getOtherParticipantId(userId);
            
            // Remove user from the room
            callRoom.removeParticipant(userId);
            
            // If room is empty, remove it
            if (callRoom.isEmpty()) {
                activeCallRooms.remove(appointmentId);
            }
            
            // Notify the other participant that this user has left
            if (otherUserId != null) {
                SocketIOClient otherClient = userSocketMap.get(otherUserId);
                if (otherClient != null) {
                    otherClient.sendEvent("user-left", new UserLeftResponse(userId, appointmentId));
                }
            }
        }
    }

    public void handleWebRTCSignal(SocketIOClient client, WebRTCSignalData data, AckRequest ackRequest) {
        Long senderId = data.getSenderId();
        Long receiverId = data.getReceiverId();
        Long appointmentId = data.getAppointmentId();
        
        logger.debug("Received {} signal from user {} to user {} for appointment {}", 
                data.getType(), senderId, receiverId, appointmentId);
        
        // Forward the signal to the receiver
        SocketIOClient receiverClient = userSocketMap.get(receiverId);
        if (receiverClient != null) {
            receiverClient.sendEvent("webrtc-signal", data);
        } else {
            logger.warn("Receiver {} is not connected, signal not delivered", receiverId);
            client.sendEvent("error", "Receiver is not connected");
        }
    }

    public void handleToggleMedia(SocketIOClient client, ToggleMediaRequest request, AckRequest ackRequest) {
        Long userId = request.getUserId();
        Long appointmentId = request.getAppointmentId();
        String mediaType = request.getMediaType(); // "audio" or "video"
        boolean enabled = request.isEnabled();
        
        logger.info("User {} toggled {} to {} for appointment {}", 
                userId, mediaType, enabled ? "enabled" : "disabled", appointmentId);
        
        CallRoom callRoom = activeCallRooms.get(appointmentId);
        if (callRoom != null) {
            // Get the other participant
            Long otherUserId = callRoom.getOtherParticipantId(userId);
            
            // Notify the other participant about media toggle
            if (otherUserId != null) {
                SocketIOClient otherClient = userSocketMap.get(otherUserId);
                if (otherClient != null) {
                    otherClient.sendEvent("media-toggled", new MediaToggledResponse(
                            userId, appointmentId, mediaType, enabled));
                }
            }
        }
    }

    private void sendErrorToClient(SocketIOClient client, String message) {
        client.sendEvent("error", message);
    }

    // Inner classes for request/response data
    
    public static class JoinCallRequest {
        private Long userId;
        private Long appointmentId;
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
    
    public static class LeaveCallRequest {
        private Long userId;
        private Long appointmentId;
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
    
    public static class WebRTCSignalData {
        private String type; // "offer", "answer", "ice-candidate"
        private Object data;
        private Long senderId;
        private Long receiverId;
        private Long appointmentId;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        
        public Long getSenderId() { return senderId; }
        public void setSenderId(Long senderId) { this.senderId = senderId; }
        
        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
    
    public static class ToggleMediaRequest {
        private Long userId;
        private Long appointmentId;
        private String mediaType; // "audio" or "video"
        private boolean enabled;
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
    
    public static class CallJoinedResponse {
        private Long appointmentId;
        private boolean peerExists;
        
        public CallJoinedResponse(Long appointmentId, boolean peerExists) {
            this.appointmentId = appointmentId;
            this.peerExists = peerExists;
        }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        
        public boolean isPeerExists() { return peerExists; }
        public void setPeerExists(boolean peerExists) { this.peerExists = peerExists; }
    }
    
    public static class UserJoinedResponse {
        private Long userId;
        private Long appointmentId;
        
        public UserJoinedResponse(Long userId, Long appointmentId) {
            this.userId = userId;
            this.appointmentId = appointmentId;
        }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
    
    public static class UserLeftResponse {
        private Long userId;
        private Long appointmentId;
        
        public UserLeftResponse(Long userId, Long appointmentId) {
            this.userId = userId;
            this.appointmentId = appointmentId;
        }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
    
    public static class MediaToggledResponse {
        private Long userId;
        private Long appointmentId;
        private String mediaType;
        private boolean enabled;
        
        public MediaToggledResponse(Long userId, Long appointmentId, String mediaType, boolean enabled) {
            this.userId = userId;
            this.appointmentId = appointmentId;
            this.mediaType = mediaType;
            this.enabled = enabled;
        }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
        
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
    }
} 