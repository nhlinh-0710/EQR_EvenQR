package com.eventqr.dto;

import java.time.LocalDateTime;

public class FeedbackResponse {
    private Long feedbackId;
    private Long eventId;
    private String eventTitle;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private String organizerReply;
    private LocalDateTime organizerReplyAt;
    private LocalDateTime createdAt;

    public FeedbackResponse() {}

    public FeedbackResponse(Long feedbackId, Long eventId, String eventTitle, Long userId, 
                           String userName, Integer rating, String comment, LocalDateTime createdAt) {
        this.feedbackId = feedbackId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Long feedbackId) {
        this.feedbackId = feedbackId;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrganizerReply() {
        return organizerReply;
    }

    public void setOrganizerReply(String organizerReply) {
        this.organizerReply = organizerReply;
    }

    public LocalDateTime getOrganizerReplyAt() {
        return organizerReplyAt;
    }

    public void setOrganizerReplyAt(LocalDateTime organizerReplyAt) {
        this.organizerReplyAt = organizerReplyAt;
    }
}

