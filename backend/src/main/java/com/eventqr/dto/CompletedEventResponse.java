package com.eventqr.dto;

import java.time.LocalDateTime;

/**
 * DTO để trả về danh sách sự kiện đã kết thúc mà user đã tham gia
 */
public class CompletedEventResponse {
    private Long eventId;
    private String title;
    private String location;
    private String imageUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long ticketId;
    private LocalDateTime registeredAt;
    private Boolean hasFeedback; // User đã feedback chưa

    public CompletedEventResponse() {}

    public CompletedEventResponse(Long eventId, String title, String location, String imageUrl,
                                 LocalDateTime startTime, LocalDateTime endTime, String status,
                                 Long ticketId, LocalDateTime registeredAt, Boolean hasFeedback) {
        this.eventId = eventId;
        this.title = title;
        this.location = location;
        this.imageUrl = imageUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.ticketId = ticketId;
        this.registeredAt = registeredAt;
        this.hasFeedback = hasFeedback;
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public Boolean getHasFeedback() {
        return hasFeedback;
    }

    public void setHasFeedback(Boolean hasFeedback) {
        this.hasFeedback = hasFeedback;
    }
}

