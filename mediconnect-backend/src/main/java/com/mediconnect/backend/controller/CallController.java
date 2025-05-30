package com.mediconnect.backend.controller;

import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.repository.AppointmentRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/calls")
@Tag(name = "Appels", description = "API de gestion des appels vidéo et audio")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CallController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Value("${socket-server.host:0.0.0.0}")
    private String socketHost;

    @Value("${socket-server.port:9092}")
    private Integer socketPort;

    @GetMapping("/connection-details/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    @Operation(summary = "Récupérer les détails de connexion pour un appel", 
               description = "Fournit les informations nécessaires pour se connecter à un appel vidéo/audio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Détails de connexion récupérés avec succès",
                     content = @Content(schema = @Schema(implementation = ConnectionDetails.class))),
        @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé", content = @Content),
        @ApiResponse(responseCode = "403", description = "Non autorisé à accéder à cet appel", content = @Content)
    })
    public ResponseEntity<?> getConnectionDetails(
            @Parameter(description = "ID du rendez-vous") @PathVariable Long appointmentId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Appointment appointment = appointmentOpt.get();
        
        // Vérifier que l'utilisateur est soit le médecin soit le patient du rendez-vous
        boolean isAuthorized = appointment.getDoctor().getEmail().equals(userEmail) || 
                               appointment.getPatient().getEmail().equals(userEmail);
        
        if (!isAuthorized) {
            return ResponseEntity.status(403).body("Non autorisé à accéder à cet appel");
        }
        
        // Créer les détails de connexion
        ConnectionDetails details = new ConnectionDetails();
        details.setAppointmentId(appointmentId);
        details.setSocketServerUrl("ws://" + socketHost + ":" + socketPort);
        details.setPatientId(appointment.getPatient().getId());
        details.setDoctorId(appointment.getDoctor().getId());
        details.setAppointmentDate(appointment.getAppointmentDate().toString());
        
        return ResponseEntity.ok(details);
    }
    
    @PostMapping("/{appointmentId}/record-metrics")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    @Operation(summary = "Enregistrer les métriques d'un appel", 
               description = "Enregistre les statistiques d'un appel (durée, qualité, etc.)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Métriques enregistrées avec succès"),
        @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé", content = @Content),
        @ApiResponse(responseCode = "403", description = "Non autorisé", content = @Content)
    })
    public ResponseEntity<?> recordCallMetrics(
            @Parameter(description = "ID du rendez-vous") @PathVariable Long appointmentId,
            @RequestBody CallMetrics metrics) {
        
        Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
        
        if (appointmentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        // Ici vous pourriez enregistrer les métriques dans une base de données
        // Pour la démo, nous retournons simplement les métriques reçues
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Métriques enregistrées avec succès");
        response.put("metrics", metrics);
        
        return ResponseEntity.ok(response);
    }
    
    @Schema(description = "Détails de connexion pour un appel")
    public static class ConnectionDetails {
        @Schema(description = "ID du rendez-vous", example = "123")
        private Long appointmentId;
        
        @Schema(description = "URL du serveur WebSocket", example = "ws://mediconnect.com:9092")
        private String socketServerUrl;
        
        @Schema(description = "ID du patient", example = "456")
        private Long patientId;
        
        @Schema(description = "ID du médecin", example = "789")
        private Long doctorId;
        
        @Schema(description = "Date et heure du rendez-vous", example = "2023-12-15T10:30:00")
        private String appointmentDate;
        
        // Getters and setters
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        
        public String getSocketServerUrl() { return socketServerUrl; }
        public void setSocketServerUrl(String socketServerUrl) { this.socketServerUrl = socketServerUrl; }
        
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        
        public String getAppointmentDate() { return appointmentDate; }
        public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }
    }
    
    @Schema(description = "Métriques d'un appel")
    public static class CallMetrics {
        @Schema(description = "Durée de l'appel en secondes", example = "300")
        private Integer duration;
        
        @Schema(description = "Score de qualité audio (1-5)", example = "4")
        private Integer audioQuality;
        
        @Schema(description = "Score de qualité vidéo (1-5)", example = "4")
        private Integer videoQuality;
        
        @Schema(description = "Type d'appel", example = "VIDEO", allowableValues = {"VIDEO", "AUDIO"})
        private String callType;
        
        @Schema(description = "Problèmes techniques rencontrés", example = "false")
        private boolean technicalIssues;
        
        // Getters and setters
        public Integer getDuration() { return duration; }
        public void setDuration(Integer duration) { this.duration = duration; }
        
        public Integer getAudioQuality() { return audioQuality; }
        public void setAudioQuality(Integer audioQuality) { this.audioQuality = audioQuality; }
        
        public Integer getVideoQuality() { return videoQuality; }
        public void setVideoQuality(Integer videoQuality) { this.videoQuality = videoQuality; }
        
        public String getCallType() { return callType; }
        public void setCallType(String callType) { this.callType = callType; }
        
        public boolean isTechnicalIssues() { return technicalIssues; }
        public void setTechnicalIssues(boolean technicalIssues) { this.technicalIssues = technicalIssues; }
    }
} 