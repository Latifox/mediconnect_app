package com.mediconnect.backend.controller;

import com.mediconnect.backend.model.Prescription;
import com.mediconnect.backend.model.User;
import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.repository.PrescriptionRepository;
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
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PrescriptionController {

    private static final Logger logger = LoggerFactory.getLogger(PrescriptionController.class);

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    /**
     * Create a new prescription (doctors only)
     */
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<?> createPrescription(@RequestBody CreatePrescriptionRequest request) {
        try {
            logger.info("Creating prescription for patient ID: {}", request.getPatientId());
            
            // Get current user (doctor)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String doctorEmail = auth.getName();
            Optional<User> doctorOpt = userRepository.findByEmail(doctorEmail);
            
            if (doctorOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Doctor not found");
            }
            
            // Get patient
            Optional<User> patientOpt = userRepository.findById(request.getPatientId());
            if (patientOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Patient not found");
            }
            
            User doctor = doctorOpt.get();
            User patient = patientOpt.get();
            
            // Create prescription
            Prescription prescription = new Prescription();
            prescription.setDoctor(doctor);
            prescription.setPatient(patient);
            prescription.setMedications(request.getMedications());
            prescription.setInstructions(request.getInstructions());
            prescription.setDiagnosis(request.getDiagnosis());
            prescription.setCreatedAt(LocalDateTime.now());
            prescription.setUpdatedAt(LocalDateTime.now());
            
            // Set appointment if provided
            if (request.getAppointmentId() != null) {
                Optional<Appointment> appointmentOpt = appointmentRepository.findById(request.getAppointmentId());
                if (appointmentOpt.isPresent()) {
                    prescription.setAppointment(appointmentOpt.get());
                }
            }
            
            Prescription savedPrescription = prescriptionRepository.save(prescription);
            logger.info("Created prescription with ID: {}", savedPrescription.getId());
            
            return ResponseEntity.ok(savedPrescription);
        } catch (Exception e) {
            logger.error("Error creating prescription: ", e);
            return ResponseEntity.internalServerError().body("Failed to create prescription");
        }
    }

    /**
     * Get prescriptions for a patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<Prescription>> getPatientPrescriptions(@PathVariable Long patientId) {
        try {
            logger.info("Fetching prescriptions for patient ID: {}", patientId);
            List<Prescription> prescriptions = prescriptionRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
            logger.info("Found {} prescriptions for patient ID: {}", prescriptions.size(), patientId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            logger.error("Error fetching patient prescriptions: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get prescriptions created by a doctor
     */
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<Prescription>> getDoctorPrescriptions(@PathVariable Long doctorId) {
        try {
            logger.info("Fetching prescriptions for doctor ID: {}", doctorId);
            List<Prescription> prescriptions = prescriptionRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId);
            logger.info("Found {} prescriptions for doctor ID: {}", prescriptions.size(), doctorId);
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            logger.error("Error fetching doctor prescriptions: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get prescription by ID
     */
    @GetMapping("/{prescriptionId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable Long prescriptionId) {
        try {
            logger.info("Fetching prescription with ID: {}", prescriptionId);
            Optional<Prescription> prescriptionOpt = prescriptionRepository.findById(prescriptionId);
            
            if (prescriptionOpt.isPresent()) {
                return ResponseEntity.ok(prescriptionOpt.get());
            } else {
                logger.warn("Prescription not found with ID: {}", prescriptionId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching prescription: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get prescriptions for a specific appointment
     */
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<Prescription>> getAppointmentPrescriptions(@PathVariable Long appointmentId) {
        try {
            logger.info("Fetching prescriptions for appointment ID: {}", appointmentId);
            List<Prescription> prescriptions = prescriptionRepository.findByAppointmentId(appointmentId);
            logger.info("Found {} prescriptions for appointment", prescriptions.size());
            return ResponseEntity.ok(prescriptions);
        } catch (Exception e) {
            logger.error("Error fetching appointment prescriptions: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update prescription
     */
    @PutMapping("/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Prescription> updatePrescription(
            @PathVariable Long prescriptionId, 
            @RequestBody UpdatePrescriptionRequest request) {
        try {
            logger.info("Updating prescription with ID: {}", prescriptionId);
            Optional<Prescription> prescriptionOpt = prescriptionRepository.findById(prescriptionId);
            
            if (prescriptionOpt.isPresent()) {
                Prescription prescription = prescriptionOpt.get();
                
                if (request.getMedications() != null) {
                    prescription.setMedications(request.getMedications());
                }
                if (request.getInstructions() != null) {
                    prescription.setInstructions(request.getInstructions());
                }
                if (request.getDiagnosis() != null) {
                    prescription.setDiagnosis(request.getDiagnosis());
                }
                
                prescription.setUpdatedAt(LocalDateTime.now());
                
                Prescription savedPrescription = prescriptionRepository.save(prescription);
                logger.info("Updated prescription successfully");
                return ResponseEntity.ok(savedPrescription);
            } else {
                logger.warn("Prescription not found with ID: {}", prescriptionId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating prescription: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // DTOs
    public static class CreatePrescriptionRequest {
        private Long patientId;
        private Long appointmentId;
        private String medications;
        private String instructions;
        private String diagnosis;

        // Getters and setters
        public Long getPatientId() { return patientId; }
        public void setPatientId(Long patientId) { this.patientId = patientId; }
        
        public Long getAppointmentId() { return appointmentId; }
        public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
        
        public String getMedications() { return medications; }
        public void setMedications(String medications) { this.medications = medications; }
        
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        
        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    }

    public static class UpdatePrescriptionRequest {
        private String medications;
        private String instructions;
        private String diagnosis;

        // Getters and setters
        public String getMedications() { return medications; }
        public void setMedications(String medications) { this.medications = medications; }
        
        public String getInstructions() { return instructions; }
        public void setInstructions(String instructions) { this.instructions = instructions; }
        
        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    }
} 