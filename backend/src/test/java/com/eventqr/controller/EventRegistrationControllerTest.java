package com.eventqr.controller;

import com.eventqr.dto.EventRegisterRequest;
import com.eventqr.dto.UserTicketResponse;
import com.eventqr.model.EventTicket;
import com.eventqr.service.EventRegistrationService;
import com.eventqr.util.ImageUrlConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventRegistrationControllerTest {

    @Mock
    private EventRegistrationService service;

    @InjectMocks
    private EventRegistrationController controller;

    @Test
    void register_shouldReturnSuccess() {
        EventRegisterRequest req = new EventRegisterRequest();
        EventTicket ticket = new EventTicket();
        ticket.setTicketId(1L);
        when(service.register(req)).thenReturn(ticket);

        var response = controller.register(req);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals(1L, body.get("ticketId"));
    }

    @Test
    void register_whenException_shouldReturnBadRequest() {
        EventRegisterRequest req = new EventRegisterRequest();
        when(service.register(req)).thenThrow(new RuntimeException("error"));

        var response = controller.register(req);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void getTickets_shouldReturnTicketsWithConvertedUrls() {
        UserTicketResponse ticket = new UserTicketResponse();
        ticket.setImageUrl("some/path.jpg");
        when(service.getTicketsOfUser(1L)).thenReturn(List.of(ticket));

        try (var mockedStatic = mockStatic(ImageUrlConverter.class)) {
            mockedStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl("some/path.jpg")).thenReturn(null);

            var response = controller.getTickets(1L);

            assertTrue(response.getStatusCode().is2xxSuccessful());
            List<UserTicketResponse> tickets = (List<UserTicketResponse>) response.getBody();
            assertNotNull(tickets);
            assertEquals(1, tickets.size());
            assertEquals("some/path.jpg", tickets.get(0).getImageUrl());
        }
    }

    @Test
    void cancelTicket_shouldReturnSuccess() {
        doNothing().when(service).cancelTicket(1L, 1L);

        var response = controller.cancelTicket(1L, 1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
    }

    @Test
    void cancelTicket_whenException_shouldReturnBadRequest() {
        doThrow(new RuntimeException("error")).when(service).cancelTicket(1L, 1L);

        var response = controller.cancelTicket(1L, 1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }
}
