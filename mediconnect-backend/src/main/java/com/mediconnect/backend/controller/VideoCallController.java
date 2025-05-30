package com.mediconnect.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Cette classe est uniquement pour la documentation Swagger des endpoints WebSocket pour les appels vidéo/audio.
 * Les endpoints ne sont pas réellement implémentés ici car ils sont gérés via des connexions WebSocket.
 */
@RestController
@RequestMapping("/api/videocall")
@Tag(name = "Appels vidéo/audio", description = "API de signalisation WebRTC pour les appels vidéo et audio")
@SecurityRequirement(name = "bearerAuth")
public class VideoCallController {

    @GetMapping("/websocket-info")
    @Operation(
        summary = "Information sur les connexions WebSocket pour appels vidéo/audio",
        description = "Fournit des informations sur la façon de se connecter au serveur WebSocket pour les appels vidéo/audio"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Information sur les endpoints WebSocket",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = WebSocketInfo.class),
                examples = @ExampleObject(
                    value = "{\n" +
                        "  \"socketServer\": \"ws://mediconnect.com:9092\",\n" +
                        "  \"authParams\": \"userId=123\",\n" +
                        "  \"events\": [\n" +
                        "    \"join-call\",\n" +
                        "    \"leave-call\",\n" +
                        "    \"webrtc-signal\",\n" +
                        "    \"toggle-media\"\n" +
                        "  ]\n" +
                        "}"
                )
            )
        )
    })
    public WebSocketInfo getWebSocketInfo() {
        // Cette méthode n'est que pour la documentation, ne pas utiliser en production
        return new WebSocketInfo();
    }
    
    /**
     * Classe de documentation pour les événements WebSocket
     */
    @Schema(description = "Documentation des événements WebSocket pour les appels")
    public static class WebSocketInfo {
        @Schema(description = "URL du serveur WebSocket", example = "ws://mediconnect.com:9092")
        private String socketServer;
        
        @Schema(description = "Paramètres d'authentification pour la connexion WebSocket", example = "userId=123")
        private String authParams;
        
        @Schema(description = "Liste des événements WebSocket disponibles")
        private String[] events;
        
        public WebSocketInfo() {
            this.socketServer = "ws://mediconnect.com:9092";
            this.authParams = "userId=<user_id>";
            this.events = new String[] {
                "join-call", "leave-call", "webrtc-signal", "toggle-media"
            };
        }
        
        public String getSocketServer() { return socketServer; }
        public void setSocketServer(String socketServer) { this.socketServer = socketServer; }
        
        public String getAuthParams() { return authParams; }
        public void setAuthParams(String authParams) { this.authParams = authParams; }
        
        public String[] getEvents() { return events; }
        public void setEvents(String[] events) { this.events = events; }
    }
    
    /**
     * Documentation des événements WebSocket
     */
    @Schema(description = "Événement pour rejoindre un appel")
    public static class JoinCallEvent {
        @Schema(description = "ID de l'utilisateur", example = "123")
        private Long userId;
        
        @Schema(description = "ID du rendez-vous", example = "456")
        private Long appointmentId;
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
    
    @Schema(description = "Événement pour quitter un appel")
    public static class LeaveCallEvent {
        @Schema(description = "ID de l'utilisateur", example = "123")
        private Long userId;
        
        @Schema(description = "ID du rendez-vous", example = "456")
        private Long appointmentId;
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
    }
    
    @Schema(description = "Événement pour envoyer un signal WebRTC")
    public static class WebRTCSignalEvent {
        @Schema(description = "Type de signal", example = "offer", allowableValues = {"offer", "answer", "ice-candidate"})
        private String type;
        
        @Schema(description = "Données du signal WebRTC")
        private Object data;
        
        @Schema(description = "ID de l'expéditeur", example = "123")
        private Long senderId;
        
        @Schema(description = "ID du destinataire", example = "456")
        private Long receiverId;
        
        @Schema(description = "ID du rendez-vous", example = "789")
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
    
    @Schema(description = "Événement pour activer/désactiver média")
    public static class ToggleMediaEvent {
        @Schema(description = "ID de l'utilisateur", example = "123")
        private Long userId;
        
        @Schema(description = "ID du rendez-vous", example = "456")
        private Long appointmentId;
        
        @Schema(description = "Type de média", example = "video", allowableValues = {"audio", "video"})
        private String mediaType;
        
        @Schema(description = "État d'activation", example = "true")
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
} 