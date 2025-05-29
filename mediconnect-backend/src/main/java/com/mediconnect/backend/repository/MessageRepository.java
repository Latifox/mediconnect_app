package com.mediconnect.backend.repository;

import com.mediconnect.backend.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT DISTINCT m FROM Message m LEFT JOIN FETCH m.sender LEFT JOIN FETCH m.receiver WHERE " +
           "(m.sender.id = :userId1 AND m.receiver.id = :userId2) OR " +
           "(m.sender.id = :userId2 AND m.receiver.id = :userId1) " +
           "ORDER BY m.sentAt ASC")
    List<Message> findConversationBetweenUsers(@Param("userId1") Long userId1, 
                                               @Param("userId2") Long userId2);

    @Query("SELECT m FROM Message m WHERE m.appointment.id = :appointmentId ORDER BY m.sentAt ASC")
    List<Message> findByAppointmentIdOrderBySentAtAsc(@Param("appointmentId") Long appointmentId);

    @Query("SELECT m FROM Message m WHERE m.appointment.id = :appointmentId ORDER BY m.sentAt ASC")
    List<Message> findByAppointmentId(@Param("appointmentId") Long appointmentId);

    // New method for finding unread messages by receiver ID
    List<Message> findByReceiverIdAndIsReadFalse(Long receiverId);

    @Query("SELECT m FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    List<Message> findUnreadMessagesByReceiver(@Param("userId") Long userId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessagesByReceiver(@Param("userId") Long userId);

    @Query("SELECT DISTINCT CASE " +
           "WHEN m.sender.id = :userId THEN m.receiver " +
           "ELSE m.sender END " +
           "FROM Message m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<Object> findConversationPartners(@Param("userId") Long userId);

    @Query("SELECT DISTINCT m FROM Message m LEFT JOIN FETCH m.sender LEFT JOIN FETCH m.receiver WHERE " +
           "m.sender.id = :userId OR m.receiver.id = :userId " +
           "ORDER BY m.sentAt DESC")
    List<Message> findMessagesByUser(@Param("userId") Long userId);
} 