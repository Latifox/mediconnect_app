package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role AND u.isActive = true")
    List<User> findActiveUsersByRole(@Param("role") User.Role role);

    @Query("SELECT u FROM User u WHERE u.role = 'DOCTOR' AND u.speciality = :speciality AND u.isActive = true")
    List<User> findDoctorsBySpeciality(@Param("speciality") String speciality);

    @Query("SELECT DISTINCT u.speciality FROM User u WHERE u.role = 'DOCTOR' AND u.speciality IS NOT NULL ORDER BY u.speciality")
    List<String> findAllSpecialities();

    @Query("SELECT u FROM User u WHERE u.role = 'DOCTOR' AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.speciality) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<User> searchDoctors(@Param("query") String query);

    @Query(value = """
        SELECT 
            (SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctorId) as total_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctorId AND a.status = 'PENDING') as pending_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctorId AND a.status = 'CONFIRMED') as confirmed_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctorId AND a.status = 'COMPLETED') as completed_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.doctor_id = :doctorId AND a.status = 'CANCELLED') as cancelled_appointments,
            (SELECT COUNT(DISTINCT a.patient_id) FROM appointments a WHERE a.doctor_id = :doctorId) as total_patients,
            (SELECT COUNT(*) FROM prescriptions p WHERE p.doctor_id = :doctorId) as total_prescriptions
        """, nativeQuery = true)
    Map<String, Object> getDoctorStats(@Param("doctorId") Long doctorId);

    @Query(value = """
        SELECT 
            (SELECT COUNT(*) FROM appointments a WHERE a.patient_id = :patientId) as total_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.patient_id = :patientId AND a.status = 'PENDING') as pending_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.patient_id = :patientId AND a.status = 'CONFIRMED') as confirmed_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.patient_id = :patientId AND a.status = 'COMPLETED') as completed_appointments,
            (SELECT COUNT(*) FROM appointments a WHERE a.patient_id = :patientId AND a.status = 'CANCELLED') as cancelled_appointments,
            (SELECT COUNT(DISTINCT a.doctor_id) FROM appointments a WHERE a.patient_id = :patientId) as total_doctors,
            (SELECT COUNT(*) FROM prescriptions p WHERE p.patient_id = :patientId) as total_prescriptions
        """, nativeQuery = true)
    Map<String, Object> getPatientStats(@Param("patientId") Long patientId);
} 