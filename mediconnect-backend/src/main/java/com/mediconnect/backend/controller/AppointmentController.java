package com.mediconnect.backend.controller;

import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.model.User;
import com.mediconnect.backend.repository.AppointmentRepository;
import com.mediconnect.backend.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Rendez-vous", description = "API de gestion des rendez-vous médicaux")
@SecurityRequirement(name = "bearerAuth")
public class AppointmentController {

    private static final Logger logger = LoggerFactory.getLogger(AppointmentController.class);

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new appointment
     */
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Créer un rendez-vous", description = "Permet à un patient de prendre rendez-vous avec un médecin")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rendez-vous créé avec succès", 
                     content = @Content(schema = @Schema(implementation = Appointment.class))),
        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<?> createAppointment(@RequestBody CreateAppointmentRequest request) {
        try {
            logger.info("Creating appointment for doctor ID: {}", request.getDoctorId());
            
            // Get current user (patient)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String patientEmail = auth.getName();
            Optional<User> patientOpt = userRepository.findByEmail(patientEmail);
            
            if (patientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Patient not found");
            }
            
            // Get doctor
            Optional<User> doctorOpt = userRepository.findById(request.getDoctorId());
            if (doctorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Doctor not found");
            }
            
            User patient = patientOpt.get();
            User doctor = doctorOpt.get();
            
            // Create appointment
            Appointment appointment = new Appointment();
            appointment.setPatient(patient);
            appointment.setDoctor(doctor);
            appointment.setAppointmentDate(request.getAppointmentDate());
            appointment.setNotes(request.getNotes());
            appointment.setStatus(Appointment.AppointmentStatus.PENDING);
            appointment.setCreatedAt(LocalDateTime.now());
            appointment.setUpdatedAt(LocalDateTime.now());
            
            Appointment savedAppointment = appointmentRepository.save(appointment);
            logger.info("Created appointment with ID: {}", savedAppointment.getId());
            
            return ResponseEntity.ok(savedAppointment);
        } catch (Exception e) {
            logger.error("Error creating appointment: ", e);
            return ResponseEntity.internalServerError().body("Failed to create appointment");
        }
    }

    /**
     * Get appointments for a doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR') or hasRole('PATIENT')")
    @Operation(summary = "Récupérer les rendez-vous d'un médecin", description = "Récupère la liste des rendez-vous pour un médecin spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des rendez-vous récupérée avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<List<Appointment>> getDoctorAppointments(
            @Parameter(description = "ID du médecin") @PathVariable Long doctorId) {
        try {
            logger.info("Fetching appointments for doctor ID: {}", doctorId);
            List<Appointment> appointments = appointmentRepository.findByDoctorIdOrderByAppointmentDateDesc(doctorId);
            logger.info("Found {} appointments for doctor ID: {}", appointments.size(), doctorId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("Error fetching doctor appointments: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get appointments for a patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    @Operation(summary = "Récupérer les rendez-vous d'un patient", description = "Récupère la liste des rendez-vous pour un patient spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des rendez-vous récupérée avec succès"),
        @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<List<Appointment>> getPatientAppointments(
            @Parameter(description = "ID du patient") @PathVariable Long patientId) {
        try {
            logger.info("Fetching appointments for patient ID: {}", patientId);
            List<Appointment> appointments = appointmentRepository.findByPatientIdOrderByAppointmentDateDesc(patientId);
            logger.info("Found {} appointments for patient ID: {}", appointments.size(), patientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            logger.error("Error fetching patient appointments: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get appointment by ID
     */
    @GetMapping("/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    @Operation(summary = "Récupérer un rendez-vous par ID", description = "Récupère les détails d'un rendez-vous spécifique")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rendez-vous trouvé"),
        @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<Appointment> getAppointmentById(
            @Parameter(description = "ID du rendez-vous") @PathVariable Long appointmentId) {
        try {
            logger.info("Fetching appointment with ID: {}", appointmentId);
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            
            if (appointmentOpt.isPresent()) {
                return ResponseEntity.ok(appointmentOpt.get());
            } else {
                logger.warn("Appointment not found with ID: {}", appointmentId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching appointment: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update appointment status
     */
    @PutMapping("/{appointmentId}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Mettre à jour le statut d'un rendez-vous", description = "Permet à un médecin de modifier le statut d'un rendez-vous")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statut mis à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @Parameter(description = "ID du rendez-vous") @PathVariable Long appointmentId, 
            @RequestBody UpdateStatusRequest request) {
        try {
            logger.info("Updating appointment {} status to: {}", appointmentId, request.getStatus());
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                appointment.setStatus(Appointment.AppointmentStatus.valueOf(request.getStatus()));
                appointment.setUpdatedAt(LocalDateTime.now());
                
                Appointment savedAppointment = appointmentRepository.save(appointment);
                logger.info("Updated appointment status successfully");
                return ResponseEntity.ok(savedAppointment);
            } else {
                logger.warn("Appointment not found with ID: {}", appointmentId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating appointment status: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Cancel appointment
     */
    @PutMapping("/{appointmentId}/cancel")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    @Operation(summary = "Annuler un rendez-vous", description = "Permet à un patient ou un médecin d'annuler un rendez-vous")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rendez-vous annulé avec succès"),
        @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<Appointment> cancelAppointment(
            @Parameter(description = "ID du rendez-vous") @PathVariable Long appointmentId) {
        try {
            logger.info("Cancelling appointment with ID: {}", appointmentId);
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
                appointment.setUpdatedAt(LocalDateTime.now());
                
                Appointment savedAppointment = appointmentRepository.save(appointment);
                logger.info("Cancelled appointment successfully");
                return ResponseEntity.ok(savedAppointment);
            } else {
                logger.warn("Appointment not found with ID: {}", appointmentId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error cancelling appointment: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update consultation notes
     */
    @PutMapping("/{appointmentId}/notes")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Mettre à jour les notes de consultation", description = "Permet à un médecin d'ajouter ou modifier les notes de consultation")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Notes mises à jour avec succès"),
        @ApiResponse(responseCode = "404", description = "Rendez-vous non trouvé", content = @Content),
        @ApiResponse(responseCode = "500", description = "Erreur serveur", content = @Content)
    })
    public ResponseEntity<Appointment> updateConsultationNotes(
            @PathVariable Long appointmentId, 
            @RequestBody UpdateNotesRequest request) {
        try {
            logger.info("Updating consultation notes for appointment ID: {}", appointmentId);
            Optional<Appointment> appointmentOpt = appointmentRepository.findById(appointmentId);
            
            if (appointmentOpt.isPresent()) {
                Appointment appointment = appointmentOpt.get();
                appointment.setConsultationNotes(request.getConsultationNotes());
                appointment.setUpdatedAt(LocalDateTime.now());
                
                Appointment savedAppointment = appointmentRepository.save(appointment);
                logger.info("Updated consultation notes successfully");
                return ResponseEntity.ok(savedAppointment);
            } else {
                logger.warn("Appointment not found with ID: {}", appointmentId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating consultation notes: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTOs
    @Schema(description = "Requête de création de rendez-vous")
    public static class CreateAppointmentRequest {
        @Schema(description = "ID du médecin", example = "1")
        private Long doctorId;
        
        @Schema(description = "Date et heure du rendez-vous", example = "2023-12-15T10:30:00")
        private LocalDateTime appointmentDate;
        
        @Schema(description = "Notes pour le rendez-vous", example = "Consultation pour maux de tête")
        private String notes;
        
        // Getters and setters
        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        
        public LocalDateTime getAppointmentDate() { return appointmentDate; }
        public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @Schema(description = "Requête de mise à jour du statut")
    public static class UpdateStatusRequest {
        @Schema(description = "Nouveau statut du rendez-vous", example = "CONFIRMED", allowableValues = {"PENDING", "CONFIRMED", "COMPLETED", "CANCELLED"})
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @Schema(description = "Requête de mise à jour des notes de consultation")
    public static class UpdateNotesRequest {
        @Schema(description = "Notes de consultation", example = "Le patient présente des symptômes de...")
        private String consultationNotes;
        
        public String getConsultationNotes() { return consultationNotes; }
        public void setConsultationNotes(String consultationNotes) { this.consultationNotes = consultationNotes; }
    }
} 