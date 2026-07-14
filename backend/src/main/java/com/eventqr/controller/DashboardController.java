package com.eventqr.controller;

import com.eventqr.dto.DashboardStatsDTO;
import com.eventqr.model.Event;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import com.eventqr.service.EventServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTicketRepository ticketRepository;

    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatsDTO> getDashboardStatistics(
            @RequestParam(required = false) Long organizerId) {
        try {
            long activeEvents;
            long totalTicketsSold;
            long totalAttendees;

            if (organizerId != null) {
                activeEvents = countActiveEventsByOrganizer(organizerId);
                totalTicketsSold = countTicketsByOrganizer(organizerId);
                totalAttendees = totalTicketsSold;
            } else {
                activeEvents = countActiveEvents();
                totalTicketsSold = ticketRepository.count();
                totalAttendees = totalTicketsSold;
            }

            DashboardStatsDTO stats = new DashboardStatsDTO(activeEvents, totalAttendees, totalTicketsSold, 0);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy thống kê dashboard: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/recent-events")
    public ResponseEntity<List<Event>> getRecentEvents(
            @RequestParam(required = false) Long organizerId,
            @RequestParam(defaultValue = "5") int limit) {
        try {
            List<Event> events = (organizerId != null)
                ? eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId)
                : eventRepository.findAll();

            LocalDateTime now = LocalDateTime.now();
            events.forEach(event -> EventServiceImpl.updateEventStatus(event, now));

            List<Event> recentEvents = events.stream()
                .sorted((e1, e2) -> {
                    if (e1.getStartTime() == null) return 1;
                    if (e2.getStartTime() == null) return -1;
                    return e1.getStartTime().compareTo(e2.getStartTime());
                })
                .limit(limit)
                .toList();

            return ResponseEntity.ok(recentEvents);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy recent events: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private long countActiveEvents() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> allEvents = eventRepository.findAll();
        allEvents.forEach(event -> EventServiceImpl.updateEventStatus(event, now));
        return allEvents.stream().filter(DashboardController::isEventActive).count();
    }

    private long countActiveEventsByOrganizer(Long organizerId) {
        LocalDateTime now = LocalDateTime.now();
        List<Event> organizerEvents = eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
        organizerEvents.forEach(event -> EventServiceImpl.updateEventStatus(event, now));
        return organizerEvents.stream().filter(DashboardController::isEventActive).count();
    }

    private static boolean isEventActive(Event event) {
        String status = event.getStatus();
        if (status == null) return false;
        status = status.toUpperCase();
        return "UPCOMING".equals(status) || "ONGOING".equals(status);
    }

    private long countTicketsByOrganizer(Long organizerId) {
        List<Event> organizerEvents = eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId);
        return organizerEvents.stream()
            .mapToLong(event -> ticketRepository.findByEventId(event.getEventId()).size())
            .sum();
    }
}

