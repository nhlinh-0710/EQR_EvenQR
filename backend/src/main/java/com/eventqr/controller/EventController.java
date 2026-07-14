package com.eventqr.controller;

import com.eventqr.dto.EventRequest;
import com.eventqr.model.Event;
import com.eventqr.service.EventService;
import com.eventqr.service.EventServiceImpl;
import com.eventqr.util.ImageUrlConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @PostMapping
    public ResponseEntity<?> createEvent(
            @ModelAttribute EventRequest eventRequest,
            @RequestHeader(value = "X-Organizer-Id", required = false) Long organizerIdHeader,
            @RequestParam(value = "organizerId", required = false) Long organizerIdParam) {
        try {
            if (eventRequest.getTitle() == null || eventRequest.getEventDate() == null || eventRequest.getLocation() == null) {
                 return new ResponseEntity<>("Vui lòng cung cấp đầy đủ thông tin bắt buộc.", HttpStatus.BAD_REQUEST);
            }

            Long organizerId = organizerIdHeader != null ? organizerIdHeader :
                              (organizerIdParam != null ? organizerIdParam : eventRequest.getOrganizerId());

            if (organizerId == null) {
                return new ResponseEntity<>(
                    Map.of("success", false, "message", "Thiếu thông tin organizerId. Vui lòng đăng nhập lại."),
                    HttpStatus.BAD_REQUEST
                );
            }

            eventRequest.setOrganizerId(organizerId);
            Event createdEvent = eventService.createEvent(eventRequest);
            prepareEventForResponse(createdEvent, LocalDateTime.now());

            return new ResponseEntity<>(createdEvent, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Map.of("success", false, "message", "Lỗi khi tạo sự kiện: " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @ModelAttribute EventRequest eventRequest,
            @RequestHeader(value = "X-Organizer-Id", required = false) Long organizerIdHeader,
            @RequestParam(value = "organizerId", required = false) Long organizerIdParam) {
        try {
            Long organizerId = organizerIdHeader != null ? organizerIdHeader : organizerIdParam;
            if (organizerId == null) {
                return new ResponseEntity<>("Thiếu thông tin organizerId", HttpStatus.BAD_REQUEST);
            }

            Event updatedEvent = eventService.updateEvent(id, eventRequest, organizerId);
            prepareEventForResponse(updatedEvent, LocalDateTime.now());

            return new ResponseEntity<>(updatedEvent, HttpStatus.OK);
        } catch (IllegalStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Lỗi khi cập nhật sự kiện: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(
            @PathVariable Long id,
            @RequestHeader(value = "X-Organizer-Id", required = false) Long organizerIdHeader,
            @RequestParam(value = "organizerId", required = false) Long organizerIdParam) {
        try {
            Event event = eventService.getEventById(id);
            prepareEventForResponse(event, LocalDateTime.now());

            Long organizerId = organizerIdHeader != null ? organizerIdHeader : organizerIdParam;
            if (organizerId != null && !event.getOrganizerId().equals(organizerId)) {
                return new ResponseEntity<>(
                    Map.of("success", false, "message", "Bạn không có quyền xem sự kiện này"),
                    HttpStatus.FORBIDDEN
                );
            }

            return new ResponseEntity<>(event, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(Map.of("success", false, "message", e.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("success", false, "message", "Lỗi khi lấy thông tin sự kiện: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<List<Event>> getAllEvents() {
        try {
            List<Event> events = eventService.findAll();
            LocalDateTime now = LocalDateTime.now();
            events.forEach(event -> prepareEventForResponse(event, now));
            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my-events")
    public ResponseEntity<?> getMyEvents(
            @RequestHeader(value = "X-Organizer-Id", required = false) Long organizerIdHeader,
            @RequestParam(value = "organizerId", required = false) Long organizerIdParam) {
        try {
            Long organizerId = organizerIdHeader != null ? organizerIdHeader : organizerIdParam;
            if (organizerId == null) {
                return new ResponseEntity<>(Map.of("success", false, "message", "Thiếu thông tin organizerId"), HttpStatus.BAD_REQUEST);
            }

            List<Event> events = eventService.findByOrganizerId(organizerId);
            LocalDateTime now = LocalDateTime.now();
            events.forEach(event -> prepareEventForResponse(event, now));

            return new ResponseEntity<>(events, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(Map.of("success", false, "message", "Lỗi khi lấy danh sách sự kiện: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void convertImageUrlToApiUrl(Event event) {
        String convertedUrl = ImageUrlConverter.convertToAccessibleUrl(event.getImageUrl());
        if (convertedUrl != null) {
            event.setImageUrl(convertedUrl);
        }
    }

    private void prepareEventForResponse(Event event, LocalDateTime now) {
        EventServiceImpl.updateEventStatus(event, now);
        convertImageUrlToApiUrl(event);
    }
}