package com.mediconnect.backend.controller;

import com.mediconnect.backend.model.User;
import com.mediconnect.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Get all doctors
     */
    @GetMapping("/doctors")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<User>> getAllDoctors() {
        try {
            logger.info("Fetching all doctors");
            List<User> doctors = userRepository.findByRole(User.Role.DOCTOR);
            logger.info("Found {} doctors", doctors.size());
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            logger.error("Error fetching doctors: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get doctors by speciality
     */
    @GetMapping("/doctors/speciality/{speciality}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<User>> getDoctorsBySpeciality(@PathVariable String speciality) {
        try {
            logger.info("Fetching doctors by speciality: {}", speciality);
            List<User> doctors = userRepository.findDoctorsBySpeciality(speciality);
            logger.info("Found {} doctors with speciality: {}", doctors.size(), speciality);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            logger.error("Error fetching doctors by speciality: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get all specialities
     */
    @GetMapping("/specialities")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<String>> getAllSpecialities() {
        try {
            logger.info("Fetching all specialities");
            List<String> specialities = userRepository.findAllSpecialities();
            logger.info("Found {} specialities", specialities.size());
            return ResponseEntity.ok(specialities);
        } catch (Exception e) {
            logger.error("Error fetching specialities: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search doctors by name or speciality
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<List<User>> searchDoctors(@RequestParam String q) {
        try {
            logger.info("Searching doctors with query: {}", q);
            List<User> doctors = userRepository.searchDoctors(q);
            logger.info("Found {} doctors matching query: {}", doctors.size(), q);
            return ResponseEntity.ok(doctors);
        } catch (Exception e) {
            logger.error("Error searching doctors: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get user profile by ID
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) {
        try {
            logger.info("Fetching user profile for ID: {}", userId);
            Optional<User> userOptional = userRepository.findById(userId);
            
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                logger.info("Found user: {} {}", user.getFirstName(), user.getLastName());
                return ResponseEntity.ok(user);
            } else {
                logger.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error fetching user profile: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Update user profile
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long userId, @RequestBody User updatedUser) {
        try {
            logger.info("Updating user profile for ID: {}", userId);
            Optional<User> userOptional = userRepository.findById(userId);
            
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                
                // Update allowed fields
                if (updatedUser.getFirstName() != null) {
                    existingUser.setFirstName(updatedUser.getFirstName());
                }
                if (updatedUser.getLastName() != null) {
                    existingUser.setLastName(updatedUser.getLastName());
                }
                if (updatedUser.getPhoneNumber() != null) {
                    existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
                }
                if (updatedUser.getMedicalHistory() != null) {
                    existingUser.setMedicalHistory(updatedUser.getMedicalHistory());
                }
                
                // For doctors, update doctor-specific fields
                if (existingUser.getRole() == User.Role.DOCTOR) {
                    if (updatedUser.getSpeciality() != null) {
                        existingUser.setSpeciality(updatedUser.getSpeciality());
                    }
                    if (updatedUser.getExperienceYears() != null) {
                        existingUser.setExperienceYears(updatedUser.getExperienceYears());
                    }
                    if (updatedUser.getConsultationFee() != null) {
                        existingUser.setConsultationFee(updatedUser.getConsultationFee());
                    }
                }
                
                User savedUser = userRepository.save(existingUser);
                logger.info("Updated user profile for: {} {}", savedUser.getFirstName(), savedUser.getLastName());
                return ResponseEntity.ok(savedUser);
            } else {
                logger.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error updating user profile: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Change password
     */
    @PutMapping("/{userId}/change-password")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<?> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequest request) {
        try {
            logger.info("Changing password for user ID: {}", userId);
            
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            // Check if the user is changing their own password
            if (!currentUser.getId().equals(userId)) {
                logger.warn("Unauthorized password change attempt for user ID: {}", userId);
                return ResponseEntity.status(403).body("You can only change your own password");
            }
            
            // Validate current password
            if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                logger.warn("Invalid current password for user ID: {}", userId);
                return ResponseEntity.badRequest().body("Current password is incorrect");
            }
            
            // Update password
            currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(currentUser);
            
            logger.info("Password changed successfully for user ID: {}", userId);
            return ResponseEntity.ok().body(Map.of("message", "Password changed successfully"));
        } catch (Exception e) {
            logger.error("Error changing password: ", e);
            return ResponseEntity.internalServerError().body("Failed to change password");
        }
    }
    
    /**
     * Get user profile stats - number of appointments, etc.
     */
    @GetMapping("/{userId}/stats")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR')")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        try {
            logger.info("Fetching stats for user ID: {}", userId);
            
            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            
            // Make sure users can only see their own stats unless they're a doctor
            if (!currentUser.getId().equals(userId) && currentUser.getRole() != User.Role.DOCTOR) {
                logger.warn("Unauthorized stats access attempt for user ID: {}", userId);
                return ResponseEntity.status(403).body("You can only access your own stats");
            }
            
            Optional<User> userOptional = userRepository.findById(userId);
            if (!userOptional.isPresent()) {
                logger.warn("User not found with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
            
            User user = userOptional.get();
            
            // Calculate stats based on user role
            Map<String, Object> stats;
            if (user.getRole() == User.Role.DOCTOR) {
                stats = userRepository.getDoctorStats(userId);
            } else {
                stats = userRepository.getPatientStats(userId);
            }
            
            logger.info("Fetched stats for user ID: {}", userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error fetching user stats: ", e);
            return ResponseEntity.internalServerError().body("Failed to fetch user stats");
        }
    }

    // DTO classes
    public static class ChangePasswordRequest {
        private String currentPassword;
        private String newPassword;

        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
} 