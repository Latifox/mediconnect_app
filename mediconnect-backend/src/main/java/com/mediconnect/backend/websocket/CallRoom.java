package com.mediconnect.backend.websocket;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a video call room for a telemedicine appointment
 */
public class CallRoom {
    private final Long appointmentId;
    private final Set<Long> participants = new HashSet<>();
    
    public CallRoom(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void addParticipant(Long userId) {
        participants.add(userId);
    }
    
    public void removeParticipant(Long userId) {
        participants.remove(userId);
    }
    
    public boolean hasParticipant(Long userId) {
        return participants.contains(userId);
    }
    
    public int getParticipantCount() {
        return participants.size();
    }
    
    public boolean isEmpty() {
        return participants.isEmpty();
    }
    
    /**
     * Get the other participant in a 1:1 call (doctor-patient)
     * @param userId The ID of the current user
     * @return The ID of the other participant, or null if none exists
     */
    public Long getOtherParticipantId(Long userId) {
        if (participants.size() != 2 || !participants.contains(userId)) {
            return null;
        }
        
        return participants.stream()
                .filter(id -> !id.equals(userId))
                .findFirst()
                .orElse(null);
    }
} 