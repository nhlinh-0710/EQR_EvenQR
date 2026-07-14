package com.eventqr.controller;

import com.eventqr.dto.CheckinResponse;
import com.eventqr.dto.CheckInHistoryResponse;
import com.eventqr.dto.QrCheckinPayload;
import com.eventqr.model.Account;
import com.eventqr.model.CheckInHistory;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.repository.AccountRepository;
import com.eventqr.repository.CheckinRepository;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class CheckInController {

    private static final Logger logger = LoggerFactory.getLogger(CheckInController.class);

    private final EventTicketRepository ticketRepo;
    private final EventRepository eventRepo;
    private final AccountRepository accountRepo;
    private final CheckinRepository checkRepo;
    private final ObjectMapper mapper;

    public CheckInController(EventTicketRepository ticketRepo, EventRepository eventRepo,
                             AccountRepository accountRepo, CheckinRepository checkRepo, ObjectMapper mapper) {
        this.ticketRepo = ticketRepo;
        this.eventRepo = eventRepo;
        this.accountRepo = accountRepo;
        this.checkRepo = checkRepo;
        this.mapper = mapper;
    }

    @GetMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestParam("payload") String payloadBase64) {
        try {
            String json = new String(Base64.getDecoder().decode(payloadBase64), StandardCharsets.UTF_8);
            QrCheckinPayload payload = mapper.readValue(json, QrCheckinPayload.class);

            EventTicket ticket = ticketRepo.findById(payload.getTicketId())
                    .orElseThrow(() -> new IllegalArgumentException("Vé không tồn tại!"));
            Event event = eventRepo.findById(payload.getEventId())
                    .orElseThrow(() -> new IllegalArgumentException("Sự kiện không tồn tại!"));

            if (!ticket.getEventId().equals(event.getEventId())) {
                throw new IllegalArgumentException("Vé không thuộc sự kiện này!");
            }

            Account user = accountRepo.findById(payload.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

            if (checkRepo.existsByTicket_TicketId(ticket.getTicketId())) {
                throw new IllegalStateException("Vé này đã check-in trước đó!");
            }

            CheckInHistory history = new CheckInHistory(ticket, payload.getUserId(), payload.getEventId());
            checkRepo.save(history);

            return ResponseEntity.ok(new CheckinResponse(event, ticket, user));
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(error(ex.getMessage()));
        } catch (Exception ex) {
            logger.error("Lỗi khi check-in: {}", ex.getMessage(), ex);
            return ResponseEntity.internalServerError().body(error("Lỗi hệ thống!"));
        }
    }

    @GetMapping("/checkin-by-code")
    public ResponseEntity<?> checkInByCode(@RequestParam("code") String code) {
        try {
            String cleaned = code.replace("#", "").trim();
            String[] parts = cleaned.split("-");
            if (parts.length != 3) throw new IllegalArgumentException("Mã QR không hợp lệ!");

            Long ticketId = Long.parseLong(parts[0]);
            Long eventId = Long.parseLong(parts[1].substring(1));
            Long userId = Long.parseLong(parts[2].substring(1));

            EventTicket ticket = ticketRepo.findById(ticketId)
                    .orElseThrow(() -> new IllegalArgumentException("Vé không tồn tại!"));
            Event event = eventRepo.findById(eventId)
                    .orElseThrow(() -> new IllegalArgumentException("Sự kiện không tồn tại!"));
            Account user = accountRepo.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

            if (!ticket.getEventId().equals(event.getEventId())) {
                throw new IllegalArgumentException("Mã QR không đúng sự kiện!");
            }
            if (checkRepo.existsByTicket_TicketId(ticketId)) {
                throw new IllegalStateException("Vé này đã check-in!");
            }

            CheckInHistory history = new CheckInHistory(ticket, userId, eventId);
            checkRepo.save(history);

            return ResponseEntity.ok(new CheckinResponse(event, ticket, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(error(e.getMessage()));
        }
    }

    @GetMapping("/checkin-history")
    public ResponseEntity<?> getCheckinHistory(
            @RequestParam("organizerId") Long organizerId,
            @RequestParam(value = "eventId", required = false) Long eventId) {
        try {
            List<CheckInHistory> histories = (eventId != null)
                ? checkRepo.findByEventIdAndOrganizerId(eventId, organizerId)
                : checkRepo.findByOrganizerId(organizerId);

            List<CheckInHistoryResponse> response = histories.stream()
                .map(history -> {
                    Event event = eventRepo.findById(history.getEventId()).orElse(null);
                    Account user = accountRepo.findById(history.getUserId()).orElse(null);
                    if (event != null && user != null) {
                        return new CheckInHistoryResponse(history, event, user);
                    }
                    return null;
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy lịch sử check-in: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(error("Lỗi khi lấy lịch sử check-in: " + e.getMessage()));
        }
    }

    private Map<String, Object> error(String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", false);
        m.put("message", msg);
        return m;
    }
}
