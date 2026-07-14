package com.eventqr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks", uniqueConstraints = {
    @UniqueConstraint(name = "uniq_event_user_feedback", columnNames = {"event_id", "user_id"})
})
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rating")
    private Integer rating; // 1-5

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "organizer_reply", columnDefinition = "TEXT")
    private String organizerReply;

    @Column(name = "organizer_reply_at")
    private LocalDateTime organizerReplyAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public Feedback() {}

    public Feedback(Long eventId, Long userId, Integer rating, String comment) {
        this.eventId = eventId;
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

