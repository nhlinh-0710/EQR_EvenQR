// src/main/java/com/eventqr/dto/QrCheckinPayload.java
package com.eventqr.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QrCheckinPayload {

    @JsonProperty("ticket_id")
    private Long ticketId;

    @JsonProperty("event_id")
    private Long eventId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("registered_at")
    private String registeredAt;

    private String nonce;

    // GETTER / SETTER
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(String registeredAt) { this.registeredAt = registeredAt; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }
}
