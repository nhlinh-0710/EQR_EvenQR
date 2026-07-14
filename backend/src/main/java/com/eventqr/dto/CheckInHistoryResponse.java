package com.eventqr.dto;

import com.eventqr.model.Account;
import com.eventqr.model.CheckInHistory;
import com.eventqr.model.Event;

import java.time.format.DateTimeFormatter;

/**
 * DTO để trả về lịch sử check-in cho organizer
 */
public class CheckInHistoryResponse {
    
    private Long checkinId;
    private Long ticketId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private Long eventId;
    private String eventName;
    private String checkinTime;  // Format: "dd/MM/yyyy HH:mm:ss"
    private String status;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    // Constructor từ CheckInHistory + các entity liên quan
    public CheckInHistoryResponse(CheckInHistory history, Event event, Account user) {
        this.checkinId = history.getId();
        this.ticketId = history.getTicket().getTicketId();
        this.userId = history.getUserId();
        this.userName = user.getName();
        this.userEmail = user.getEmail();
        this.userPhone = user.getPhone();
        this.eventId = history.getEventId();
        this.eventName = event.getTitle();
        this.checkinTime = history.getCheckedAt().format(FORMATTER);
        this.status = "CHECKED_IN";
    }
    
    // Getters
    public Long getCheckinId() { return checkinId; }
    public Long getTicketId() { return ticketId; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getUserPhone() { return userPhone; }
    public Long getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public String getCheckinTime() { return checkinTime; }
    public String getStatus() { return status; }
    
    // Setters
    public void setCheckinId(Long checkinId) { this.checkinId = checkinId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public void setCheckinTime(String checkinTime) { this.checkinTime = checkinTime; }
    public void setStatus(String status) { this.status = status; }
}

