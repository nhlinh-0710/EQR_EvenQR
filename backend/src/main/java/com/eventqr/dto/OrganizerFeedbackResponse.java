package com.eventqr.dto;

/**
 * DTO cho response khi lấy danh sách feedback của organizer
 * Format theo yêu cầu:
 * {
 *   "eventId": 12,
 *   "eventName": "Tech Innovation 2025",
 *   "userName": "Nguyễn Văn A",
 *   "rating": 5,
 *   "comment": "Hay!",
 *   "time": "2025-11-20 14:22"
 * }
 */
public class OrganizerFeedbackResponse {
    private Long eventId;
    private String eventName;
    private String userName;
    private Integer rating;
    private String comment;
    private String time; // Format: "2025-11-20 14:22"

    public OrganizerFeedbackResponse() {}

    public OrganizerFeedbackResponse(Long eventId, String eventName, String userName, 
                                    Integer rating, String comment, String time) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.userName = userName;
        this.rating = rating;
        this.comment = comment;
        this.time = time;
    }

    // Getters and Setters
    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

