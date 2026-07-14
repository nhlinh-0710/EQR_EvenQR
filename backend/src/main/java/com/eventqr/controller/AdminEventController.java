package com.eventqr.controller;

import com.eventqr.repository.CheckinRepository;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import com.eventqr.repository.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/events")
public class AdminEventController {

    private final EventRepository eventRepository;
    private final FeedbackRepository feedbackRepository;
    private final EventTicketRepository eventTicketRepository;
    private final CheckinRepository checkinRepository;

    @Autowired
    public AdminEventController(EventRepository eventRepository,
                                FeedbackRepository feedbackRepository,
                                EventTicketRepository eventTicketRepository,
                                CheckinRepository checkinRepository) {
        this.eventRepository = eventRepository;
        this.feedbackRepository = feedbackRepository;
        this.eventTicketRepository = eventTicketRepository;
        this.checkinRepository = checkinRepository;
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {

        if (!eventRepository.existsById(eventId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "success", false,
                            "message", "Không tìm thấy sự kiện có id = " + eventId
                    ));
        }

        
        feedbackRepository.deleteByEventId(eventId);
        checkinRepository.deleteByEventId(eventId);
        eventTicketRepository.deleteByEventId(eventId);

        
        eventRepository.deleteById(eventId);

        return ResponseEntity.ok(
                Map.of(
                        "success", true,
                        "message", "Đã xoá sự kiện và toàn bộ dữ liệu liên quan thành công"
                )
        );
    }
}
