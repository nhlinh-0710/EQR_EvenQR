package com.eventqr.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkin_history")
public class CheckInHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private EventTicket ticket;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkedAt;

    public CheckInHistory() {}

    public CheckInHistory(EventTicket ticket, Long userId, Long eventId) {
        this.ticket = ticket;
        this.userId = userId;
        this.eventId = eventId;
        this.checkedAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public EventTicket getTicket() { return ticket; }
    public void setTicket(EventTicket ticket) { this.ticket = ticket; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public LocalDateTime getCheckedAt() { return checkedAt; }
    public void setCheckedAt(LocalDateTime checkedAt) { this.checkedAt = checkedAt; }
}

