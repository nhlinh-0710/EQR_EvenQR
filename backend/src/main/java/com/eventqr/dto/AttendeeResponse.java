package com.eventqr.dto;

import java.time.LocalDateTime;

/**
 * DTO để trả về thông tin người tham dự cho organizer
 */
public class AttendeeResponse {
    
    private Long ticketId;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String occupation;
    private String ticketType;
    private String note;
    private LocalDateTime registeredAt;
    private String status;  // PENDING, REGISTERED, CHECKED_IN
    private LocalDateTime checkedInAt;  // Null nếu chưa check-in
    private Boolean cancelled;
    
    // Constructors
    public AttendeeResponse() {}
    
    public AttendeeResponse(Long ticketId, Long userId, String name, String email, 
                           String phone, String occupation, String ticketType, 
                           String note, LocalDateTime registeredAt, String status,
                           LocalDateTime checkedInAt, Boolean cancelled) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.occupation = occupation;
        this.ticketType = ticketType;
        this.note = note;
        this.registeredAt = registeredAt;
        this.status = status;
        this.checkedInAt = checkedInAt;
        this.cancelled = cancelled;
    }
    
    // Getters and Setters
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }
    
    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }
    
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(LocalDateTime checkedInAt) { this.checkedInAt = checkedInAt; }
    
    public Boolean getCancelled() { return cancelled; }
    public void setCancelled(Boolean cancelled) { this.cancelled = cancelled; }
}

