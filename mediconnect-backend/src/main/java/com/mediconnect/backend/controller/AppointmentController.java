package com.mediconnect.backend.controller;

import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.model.User;
import com.mediconnect.backend.repository.AppointmentRepository;
import com.mediconnect.backend.repository.UserRepository;
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
    public ResponseEntity<List<Appointment>> getDoctorAppointments(@PathVariable Long doctorId) {
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
    public ResponseEntity<List<Appointment>> getPatientAppointments(@PathVariable Long patientId) {
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
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long appointmentId) {
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
    public ResponseEntity<Appointment> updateAppointmentStatus(
            @PathVariable Long appointmentId, 
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
    public ResponseEntity<Appointment> cancelAppointment(@PathVariable Long appointmentId) {
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
    public static class CreateAppointmentRequest {
        private Long doctorId;
        private LocalDateTime appointmentDate;
        private String notes;

        // Getters and setters
        public Long getDoctorId() { return doctorId; }
        public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }
        
        public LocalDateTime getAppointmentDate() { return appointmentDate; }
        public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class UpdateStatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    public static class UpdateNotesRequest {
        private String consultationNotes;

        public String getConsultationNotes() { return consultationNotes; }
        public void setConsultationNotes(String consultationNotes) { this.consultationNotes = consultationNotes; }
    }
} 