package com.mediconnect.backend.service;

import com.mediconnect.backend.dto.RegisterRequest;
import com.mediconnect.backend.model.User;
import com.mediconnect.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public User createUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());

        // Set role-specific fields
        if (request.getRole() == User.Role.DOCTOR) {
            user.setSpeciality(request.getSpeciality());
            user.setLicenseNumber(request.getLicenseNumber());
            user.setExperienceYears(request.getExperienceYears());
            user.setConsultationFee(request.getConsultationFee());
        } else if (request.getRole() == User.Role.PATIENT) {
            user.setDateOfBirth(request.getDateOfBirth());
            user.setMedicalHistory(request.getMedicalHistory());
        }

        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAllDoctors() {
        return userRepository.findActiveUsersByRole(User.Role.DOCTOR);
    }

    public List<User> findDoctorsBySpeciality(String speciality) {
        return userRepository.findDoctorsBySpeciality(speciality);
    }

    public List<String> findAllSpecialities() {
        return userRepository.findAllSpecialities();
    }

    public List<User> searchDoctors(String searchTerm) {
        return userRepository.searchDoctors(searchTerm);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName(updatedUser.getLastName());
                    user.setPhoneNumber(updatedUser.getPhoneNumber());
                    
                    if (user.getRole() == User.Role.DOCTOR) {
                        user.setSpeciality(updatedUser.getSpeciality());
                        user.setExperienceYears(updatedUser.getExperienceYears());
                        user.setConsultationFee(updatedUser.getConsultationFee());
                    } else if (user.getRole() == User.Role.PATIENT) {
                        user.setDateOfBirth(updatedUser.getDateOfBirth());
                        user.setMedicalHistory(updatedUser.getMedicalHistory());
                    }
                    
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public void deleteUser(Long id) {
        userRepository.findById(id)
                .map(user -> {
                    user.setIsActive(false);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
} 