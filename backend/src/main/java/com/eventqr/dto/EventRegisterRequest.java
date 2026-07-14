package com.eventqr.dto;

public class EventRegisterRequest {

    private Long eventId;
    private Long userId;      // 🔥 ĐỔI Integer -> Long
    private String name;
    private String email;
    private String phone;
    private String occupation;
    private String ticketType;
    private String note;

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

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
}
