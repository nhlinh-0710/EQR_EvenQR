package com.eventqr.dto;

/**
 * DTO cho thống kê người tham dự của một sự kiện
 */
public class AttendeeStatistics {
    
    private Long eventId;
    private String eventTitle;
    private int totalRegistered;      // Tổng số người đăng ký (không bao gồm cancelled)
    private int totalCheckedIn;       // Số người đã check-in
    private int totalPending;         // Số người chưa xác nhận
    private int totalCancelled;       // Số người đã hủy
    private double checkInRate;       // Tỷ lệ check-in (%)
    
    // Constructors
    public AttendeeStatistics() {}
    
    public AttendeeStatistics(Long eventId, String eventTitle, int totalRegistered, 
                             int totalCheckedIn, int totalPending, int totalCancelled) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.totalRegistered = totalRegistered;
        this.totalCheckedIn = totalCheckedIn;
        this.totalPending = totalPending;
        this.totalCancelled = totalCancelled;
        
        // Tính tỷ lệ check-in
        if (totalRegistered > 0) {
            this.checkInRate = (double) totalCheckedIn / totalRegistered * 100;
        } else {
            this.checkInRate = 0.0;
        }
    }
    
    // Getters and Setters
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    
    public String getEventTitle() { return eventTitle; }
    public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }
    
    public int getTotalRegistered() { return totalRegistered; }
    public void setTotalRegistered(int totalRegistered) { this.totalRegistered = totalRegistered; }
    
    public int getTotalCheckedIn() { return totalCheckedIn; }
    public void setTotalCheckedIn(int totalCheckedIn) { this.totalCheckedIn = totalCheckedIn; }
    
    public int getTotalPending() { return totalPending; }
    public void setTotalPending(int totalPending) { this.totalPending = totalPending; }
    
    public int getTotalCancelled() { return totalCancelled; }
    public void setTotalCancelled(int totalCancelled) { this.totalCancelled = totalCancelled; }
    
    public double getCheckInRate() { return checkInRate; }
    public void setCheckInRate(double checkInRate) { this.checkInRate = checkInRate; }
}

