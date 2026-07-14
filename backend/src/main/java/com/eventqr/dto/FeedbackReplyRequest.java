package com.eventqr.dto;

public class FeedbackReplyRequest {
    private Long organizerId;
    private String reply;

    public FeedbackReplyRequest() {}

    public FeedbackReplyRequest(Long organizerId, String reply) {
        this.organizerId = organizerId;
        this.reply = reply;
    }

    public Long getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(Long organizerId) {
        this.organizerId = organizerId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}

