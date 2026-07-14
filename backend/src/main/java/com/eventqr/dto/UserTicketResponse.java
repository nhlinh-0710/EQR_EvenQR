package com.eventqr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class UserTicketResponse {

    private Long ticketId;
    private Long eventId;
    private String eventTitle;
    private String location;
    private String imageUrl;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String ticketType;
    private LocalDateTime registeredAt;
    private String phone;

    @JsonProperty("eventStatus")
    private String eventStatus; // Status của sự kiện (DRAFT, UPCOMING, ONGOING, COMPLETED, CANCELLED)

    @JsonProperty("cancelled")
    private Boolean cancelled; // Vé có bị user hủy không (dùng cancelled thay vì isCancelled để tránh Jackson bỏ prefix "is")

    public UserTicketResponse() {
    }

    public UserTicketResponse(Long ticketId, Long eventId, String eventTitle,
            String location, String imageUrl,
            LocalDateTime startTime, LocalDateTime endTime,
            String ticketType, LocalDateTime registeredAt,
            String eventStatus, Boolean cancelled) {
        this.ticketId = ticketId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.location = location;
        this.imageUrl = imageUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.ticketType = ticketType;
        this.registeredAt = registeredAt;
        this.eventStatus = eventStatus;
        this.cancelled = cancelled;
    }

    // Getter + Setter (giữ vậy là đủ)
    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
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

    public String getTicketType() {
        return ticketType;
    }

    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public Boolean getCancelled() {
        return cancelled;
    }

    public void setCancelled(Boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Alias cho compatibility
    public Boolean getIsCancelled() {
        return cancelled;
    }

    public void setIsCancelled(Boolean isCancelled) {
        this.cancelled = isCancelled;
    }
}
