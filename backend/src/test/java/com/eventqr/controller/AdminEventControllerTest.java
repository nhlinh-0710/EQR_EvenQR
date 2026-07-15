package com.eventqr.controller;

import com.eventqr.repository.CheckinRepository;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import com.eventqr.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminEventControllerTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private EventTicketRepository eventTicketRepository;

    @Mock
    private CheckinRepository checkinRepository;

    @InjectMocks
    private AdminEventController controller;

    @Test
    void deleteEvent_whenEventExists_shouldDeleteAndReturnOk() {
        when(eventRepository.existsById(1L)).thenReturn(true);

        var response = controller.deleteEvent(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        verify(feedbackRepository).deleteByEventId(1L);
        verify(checkinRepository).deleteByEventId(1L);
        verify(eventTicketRepository).deleteByEventId(1L);
        verify(eventRepository).deleteById(1L);
    }

    @Test
    void deleteEvent_whenEventNotFound_shouldReturn404() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        var response = controller.deleteEvent(1L);

        assertEquals(404, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        verifyNoInteractions(feedbackRepository, checkinRepository, eventTicketRepository);
        verify(eventRepository, never()).deleteById(any());
    }
}
