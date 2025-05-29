package com.mediconnect.backend.controller;

import com.mediconnect.backend.model.Message;
import com.mediconnect.backend.model.User;
import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.repository.MessageRepository;
import com.mediconnect.backend.repository.UserRepository;
import com.mediconnect.backend.repository.AppointmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Send a new message
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            logger.info("Sending message from user to user ID: {}", request.getReceiverId());
            
            // Get current user (sender)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String senderEmail = auth.getName();
            Optional<User> senderOpt = userRepository.findByEmail(senderEmail);
            
            if (senderOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Sender not found");
            }
            
            // Get receiver
            Optional<User> receiverOpt = userRepository.findById(request.getReceiverId());
            if (receiverOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Receiver not found");
            }
            
            User sender = senderOpt.get();
            User receiver = receiverOpt.get();
            
            // Create message
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(request.getContent());
            message.setIsRead(false);
            message.setSentAt(LocalDateTime.now());
            
            if (request.getAppointmentId() != null) {
                // Get and set appointment if provided
                Optional<Appointment> appointmentOpt = appointmentRepository.findById(request.getAppointmentId());
                if (appointmentOpt.isPresent()) {
                    message.setAppointment(appointmentOpt.get());
                }
            }
            
            Message savedMessage = messageRepository.save(message);
            logger.info("Sent message with ID: {}", savedMessage.getId());
            
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            logger.error("Error sending message: ", e);
            return ResponseEntity.internalServerError().body("Failed to send message");
        }
    }

    /**
     * Get conversation between current user and another user
     */
    @GetMapping("/conversation/{userId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<Message>> getConversation(@PathVariable Long userId) {
        try {
            logger.info("Fetching conversation with user ID: {}", userId);
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
            
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Long currentUserId = currentUserOpt.get().getId();
            List<Message> messages = messageRepository.findConversationBetweenUsers(currentUserId, userId);
            
            logger.info("Found {} messages in conversation", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error fetching conversation: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get unread messages for current user
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<Message>> getUnreadMessages() {
        try {
            logger.info("Fetching unread messages");
            
            // Get current user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserEmail = auth.getName();
            Optional<User> currentUserOpt = userRepository.findByEmail(currentUserEmail);
            
            if (currentUserOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            Long currentUserId = currentUserOpt.get().getId();
            List<Message> unreadMessages = messageRepository.findByReceiverIdAndIsReadFalse(currentUserId);
            
            logger.info("Found {} unread messages", unreadMessages.size());
            return ResponseEntity.ok(unreadMessages);
        } catch (Exception e) {
            logger.error("Error fetching unread messages: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Mark message as read
     */
    @PutMapping("/{messageId}/read")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {
        try {
            logger.info("Marking message {} as read", messageId);
            
            Optional<Message> messageOpt = messageRepository.findById(messageId);
            if (messageOpt.isPresent()) {
                Message message = messageOpt.get();
                message.setIsRead(true);
                messageRepository.save(message);
                
                logger.info("Marked message as read successfully");
                return ResponseEntity.ok().build();
            } else {
                logger.warn("Message not found with ID: {}", messageId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error marking message as read: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get messages for a specific appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<Message>> getAppointmentMessages(@PathVariable Long appointmentId) {
        try {
            logger.info("Fetching messages for appointment ID: {}", appointmentId);
            List<Message> messages = messageRepository.findByAppointmentId(appointmentId);
            logger.info("Found {} messages for appointment", messages.size());
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error fetching appointment messages: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTOs
    public static class SendMessageRequest {
        private Long receiverId;
        private String content;
        private Long appointmentId;

        // Getters and setters
        public Long getReceiverId() { return receiverId; }
        public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
} 