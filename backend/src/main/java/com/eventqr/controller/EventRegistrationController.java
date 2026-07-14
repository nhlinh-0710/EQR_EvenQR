package com.eventqr.controller;

import com.eventqr.dto.EventRegisterRequest;
import com.eventqr.dto.UserTicketResponse;
import com.eventqr.model.EventTicket;
import com.eventqr.service.EventRegistrationService;
import com.eventqr.util.ImageUrlConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EventRegistrationController {

    private final EventRegistrationService service;

    public EventRegistrationController(EventRegistrationService service) {
        this.service = service;
    }

    // ==========================
    // POST /api/events/register
    // ==========================
    // LINH
    @PostMapping("/events/register")
    public ResponseEntity<?> register(@RequestBody EventRegisterRequest req) {
        try {
            EventTicket ticket = service.register(req);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "ticketId", ticket.getTicketId(),
                    "message", "Đăng ký thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    // LINH

    // ==========================
    // GET /api/user/{userId}/tickets
    // ==========================
    @GetMapping("/user/{userId}/tickets")
    public ResponseEntity<?> getTickets(@PathVariable Long userId) {
        List<UserTicketResponse> tickets = service.getTicketsOfUser(userId);
        
        // Convert image URLs cho tất cả tickets
        tickets.forEach(ticket -> {
            String convertedUrl = ImageUrlConverter.convertToAccessibleUrl(ticket.getImageUrl());
            if (convertedUrl != null) {
                ticket.setImageUrl(convertedUrl);
            }
        });
        
        return ResponseEntity.ok(tickets);
    }

    // ==========================
    // DELETE /api/user/{userId}/tickets/{ticketId}
    // ==========================
    @DeleteMapping("/user/{userId}/tickets/{ticketId}")
    public ResponseEntity<?> cancelTicket(
            @PathVariable Long userId,
            @PathVariable Long ticketId
    ) {
        try {
            service.cancelTicket(userId, ticketId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Hủy vé thành công!"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
