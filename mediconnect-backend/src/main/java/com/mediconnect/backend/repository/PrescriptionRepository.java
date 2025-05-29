package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    List<Prescription> findByDoctorId(Long doctorId);

    List<Prescription> findByPatientId(Long patientId);

    List<Prescription> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);

    List<Prescription> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    @Query("SELECT p FROM Prescription p WHERE p.appointment.id = :appointmentId")
    List<Prescription> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId ORDER BY p.prescribedDate DESC")
    List<Prescription> findByPatientIdOrderByDateDesc(@Param("patientId") Long patientId);

    @Query("SELECT p FROM Prescription p WHERE p.doctor.id = :doctorId ORDER BY p.prescribedDate DESC")
    List<Prescription> findByDoctorIdOrderByDateDesc(@Param("doctorId") Long doctorId);

    @Query("SELECT p FROM Prescription p WHERE p.validUntil >= :currentDate")
    List<Prescription> findValidPrescriptions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT p FROM Prescription p WHERE p.patient.id = :patientId AND p.validUntil >= :currentDate")
    List<Prescription> findValidPrescriptionsByPatient(@Param("patientId") Long patientId,
                                                       @Param("currentDate") LocalDateTime currentDate);
} 