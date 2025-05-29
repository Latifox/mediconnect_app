package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.Appointment;
import com.mediconnect.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorIdOrderByAppointmentDateDesc(Long doctorId);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);

    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = :status")
    List<Appointment> findByDoctorIdAndStatus(@Param("doctorId") Long doctorId, 
                                              @Param("status") Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.status = :status")
    List<Appointment> findByPatientIdAndStatus(@Param("patientId") Long patientId, 
                                               @Param("status") Appointment.AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND " +
           "a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByDoctorIdAndDateRange(@Param("doctorId") Long doctorId,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND " +
           "a.appointmentDate BETWEEN :startDate AND :endDate")
    List<Appointment> findByPatientIdAndDateRange(@Param("patientId") Long patientId,
                                                  @Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND " +
           "DATE(a.appointmentDate) = DATE(:date)")
    List<Appointment> findByDoctorIdAndDate(@Param("doctorId") Long doctorId,
                                            @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE a.doctor.id = :doctorId AND " +
           "a.appointmentDate = :appointmentDate AND a.status != 'CANCELLED'")
    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(@Param("doctorId") Long doctorId,
                                                           @Param("appointmentDate") LocalDateTime appointmentDate);

    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate BETWEEN :startTime AND :endTime AND " +
           "a.status = 'CONFIRMED'")
    List<Appointment> findUpcomingAppointments(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);
} 