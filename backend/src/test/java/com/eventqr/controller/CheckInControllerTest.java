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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckInControllerTest {

    @Mock
    private EventTicketRepository ticketRepo;

    @Mock
    private EventRepository eventRepo;

    @Mock
    private AccountRepository accountRepo;

    @Mock
    private CheckinRepository checkRepo;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private CheckInController controller;

    private EventTicket ticket;
    private Event event;
    private Account user;
    private QrCheckinPayload payload;
    private final Long ticketId = 1L;
    private final Long eventId = 10L;
    private final Long userId = 100L;
    private final Long organizerId = 50L;
    private String validBase64Payload;

    @BeforeEach
    void setUp() throws Exception {
        ticket = new EventTicket();
        ticket.setTicketId(ticketId);
        ticket.setEventId(eventId);
        ticket.setUserId(userId);
        ticket.setName("Test User");
        ticket.setEmail("test@example.com");
        ticket.setPhone("0123456789");

        event = new Event();
        event.setEventId(eventId);
        event.setOrganizerId(organizerId);
        event.setTitle("Test Event");

        user = new Account();
        user.setId(userId);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPhone("0123456789");

        payload = new QrCheckinPayload();
        payload.setTicketId(ticketId);
        payload.setEventId(eventId);
        payload.setUserId(userId);

        String json = "{\"ticket_id\":1,\"event_id\":10,\"user_id\":100}";
        validBase64Payload = Base64.getEncoder().encodeToString(json.getBytes());
    }

    @Test
    void checkIn_success() {
        when(mapper.readValue(anyString(), eq(QrCheckinPayload.class))).thenReturn(payload);
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));
        when(checkRepo.existsByTicket_TicketId(ticketId)).thenReturn(false);

        ResponseEntity<?> response = controller.checkIn(validBase64Payload);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(CheckinResponse.class, response.getBody());
        verify(checkRepo).save(any(CheckInHistory.class));
    }

    @Test
    void checkIn_base64DecodeError() {
        ResponseEntity<?> response = controller.checkIn("!!!invalid-base64!!!");

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void checkIn_ticketNotFound() {
        when(mapper.readValue(anyString(), eq(QrCheckinPayload.class))).thenReturn(payload);
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.checkIn(validBase64Payload);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("Vé không tồn tại!", body.get("message"));
    }

    @Test
    void checkIn_eventNotFound() {
        when(mapper.readValue(anyString(), eq(QrCheckinPayload.class))).thenReturn(payload);
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.checkIn(validBase64Payload);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Sự kiện không tồn tại!", body.get("message"));
    }

    @Test
    void checkIn_ticketNotBelongToEvent() {
        ticket.setEventId(999L);
        when(mapper.readValue(anyString(), eq(QrCheckinPayload.class))).thenReturn(payload);
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));

        ResponseEntity<?> response = controller.checkIn(validBase64Payload);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Vé không thuộc sự kiện này!", body.get("message"));
    }

    @Test
    void checkIn_userNotFound() {
        when(mapper.readValue(anyString(), eq(QrCheckinPayload.class))).thenReturn(payload);
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.checkIn(validBase64Payload);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Người dùng không tồn tại!", body.get("message"));
    }

    @Test
    void checkIn_alreadyCheckedIn() {
        when(mapper.readValue(anyString(), eq(QrCheckinPayload.class))).thenReturn(payload);
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));
        when(checkRepo.existsByTicket_TicketId(ticketId)).thenReturn(true);

        ResponseEntity<?> response = controller.checkIn(validBase64Payload);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Vé này đã check-in trước đó!", body.get("message"));
    }

    @Test
    void checkIn_genericException() {
        when(mapper.readValue(anyString(), eq(QrCheckinPayload.class))).thenThrow(new RuntimeException("Lỗi hệ thống!"));

        ResponseEntity<?> response = controller.checkIn(validBase64Payload);

        assertEquals(500, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Lỗi hệ thống!", body.get("message"));
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void checkInByCode_success() {
        String code = ticketId + "-E" + eventId + "-U" + userId;
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));
        when(checkRepo.existsByTicket_TicketId(ticketId)).thenReturn(false);

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(CheckinResponse.class, response.getBody());
        verify(checkRepo).save(any(CheckInHistory.class));
    }

    @Test
    void checkInByCode_invalidQrFormat() {
        String code = "invalid-format";

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Mã QR không hợp lệ!", body.get("message"));
    }

    @Test
    void checkInByCode_ticketNotFound() {
        String code = ticketId + "-E" + eventId + "-U" + userId;
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Vé không tồn tại!", body.get("message"));
    }

    @Test
    void checkInByCode_eventNotFound() {
        String code = ticketId + "-E" + eventId + "-U" + userId;
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Sự kiện không tồn tại!", body.get("message"));
    }

    @Test
    void checkInByCode_userNotFound() {
        String code = ticketId + "-E" + eventId + "-U" + userId;
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Người dùng không tồn tại!", body.get("message"));
    }

    @Test
    void checkInByCode_wrongEvent() {
        ticket.setEventId(999L);
        String code = ticketId + "-E" + eventId + "-U" + userId;
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Mã QR không đúng sự kiện!", body.get("message"));
    }

    @Test
    void checkInByCode_alreadyCheckedIn() {
        String code = ticketId + "-E" + eventId + "-U" + userId;
        when(ticketRepo.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));
        when(checkRepo.existsByTicket_TicketId(ticketId)).thenReturn(true);

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Vé này đã check-in!", body.get("message"));
    }

    @Test
    void checkInByCode_genericException() {
        String code = ticketId + "-E" + eventId + "-U" + userId;
        when(ticketRepo.findById(ticketId)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = controller.checkInByCode(code);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("DB error", body.get("message"));
    }

    @Test
    void getCheckinHistory_successWithEventId() {
        CheckInHistory history = new CheckInHistory(ticket, userId, eventId);
        history.setId(1L);

        when(checkRepo.findByEventIdAndOrganizerId(eventId, organizerId)).thenReturn(List.of(history));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.getCheckinHistory(organizerId, eventId);

        assertEquals(200, response.getStatusCode().value());
        List<CheckInHistoryResponse> body = (List<CheckInHistoryResponse>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(eventId, body.get(0).getEventId());
        assertEquals(userId, body.get(0).getUserId());
    }

    @Test
    void getCheckinHistory_successWithoutEventId() {
        CheckInHistory history = new CheckInHistory(ticket, userId, eventId);
        history.setId(1L);

        when(checkRepo.findByOrganizerId(organizerId)).thenReturn(List.of(history));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.getCheckinHistory(organizerId, null);

        assertEquals(200, response.getStatusCode().value());
        List<CheckInHistoryResponse> body = (List<CheckInHistoryResponse>) response.getBody();
        assertEquals(1, body.size());
        verify(checkRepo).findByOrganizerId(organizerId);
        verify(checkRepo, never()).findByEventIdAndOrganizerId(any(), any());
    }

    @Test
    void getCheckinHistory_eventOrUserNullFilteredOut() {
        CheckInHistory history1 = new CheckInHistory(ticket, userId, eventId);
        history1.setId(1L);
        CheckInHistory history2 = new CheckInHistory(ticket, 999L, 888L);
        history2.setId(2L);

        when(checkRepo.findByOrganizerId(organizerId)).thenReturn(List.of(history1, history2));
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(event));
        when(accountRepo.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepo.findById(888L)).thenReturn(Optional.empty());
        when(accountRepo.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.getCheckinHistory(organizerId, null);

        assertEquals(200, response.getStatusCode().value());
        List<CheckInHistoryResponse> body = (List<CheckInHistoryResponse>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(eventId, body.get(0).getEventId());
    }

    @Test
    void getCheckinHistory_exception() {
        when(checkRepo.findByOrganizerId(organizerId)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = controller.getCheckinHistory(organizerId, null);

        assertEquals(400, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Lỗi khi lấy lịch sử check-in: DB error", body.get("message"));
        assertFalse((Boolean) body.get("success"));
    }
}
