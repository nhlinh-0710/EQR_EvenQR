package com.eventqr.controller;

import com.eventqr.model.Event;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import com.eventqr.service.EventServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventTicketRepository ticketRepository;

    @InjectMocks
    private DashboardController controller;

    @Test
    void getDashboardStatistics_withOrganizerId_shouldReturnStats() {
        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setStatus("UPCOMING");
        Event event2 = new Event();
        event2.setEventId(2L);
        event2.setStatus("COMPLETED");
        List<Event> events = List.of(event1, event2);

        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(1L)).thenReturn(events);
        when(ticketRepository.findByEventId(1L)).thenReturn(List.of(mock(), mock(), mock(), mock(), mock()));
        when(ticketRepository.findByEventId(2L)).thenReturn(List.of(mock(), mock(), mock()));

        try (var mockedStatic = mockStatic(EventServiceImpl.class)) {
            var response = controller.getDashboardStatistics(1L);

            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getActiveEvents());
            assertEquals(8, response.getBody().getTotalTicketsSold());
            assertEquals(8, response.getBody().getTotalAttendees());
        }
    }

    @Test
    void getDashboardStatistics_withoutOrganizerId_shouldReturnStats() {
        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setStatus("UPCOMING");
        Event event2 = new Event();
        event2.setEventId(2L);
        event2.setStatus("COMPLETED");

        when(eventRepository.findAll()).thenReturn(List.of(event1, event2));
        when(ticketRepository.count()).thenReturn(10L);

        try (var mockedStatic = mockStatic(EventServiceImpl.class)) {
            var response = controller.getDashboardStatistics(null);

            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getActiveEvents());
            assertEquals(10, response.getBody().getTotalTicketsSold());
            assertEquals(10, response.getBody().getTotalAttendees());
        }
    }

    @Test
    void getDashboardStatistics_whenException_shouldReturn500() {
        when(eventRepository.findAll()).thenThrow(new RuntimeException("DB error"));

        var response = controller.getDashboardStatistics(null);

        assertTrue(response.getStatusCode().is5xxServerError());
    }

    @Test
    void getRecentEvents_withOrganizerId_shouldReturnSortedEvents() {
        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setStartTime(LocalDateTime.now().plusDays(5));
        event1.setStatus("UPCOMING");
        Event event2 = new Event();
        event2.setEventId(2L);
        event2.setStartTime(LocalDateTime.now().plusDays(1));
        event2.setStatus("UPCOMING");

        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(event1, event2));

        try (var mockedStatic = mockStatic(EventServiceImpl.class)) {
            var response = controller.getRecentEvents(1L, 5);

            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().size());
            assertEquals(2L, response.getBody().get(0).getEventId());
            assertEquals(1L, response.getBody().get(1).getEventId());
        }
    }

    @Test
    void getRecentEvents_withoutOrganizerId_shouldReturnAllEvents() {
        Event event1 = new Event();
        event1.setEventId(1L);
        event1.setStartTime(LocalDateTime.now().plusDays(3));
        event1.setStatus("UPCOMING");

        when(eventRepository.findAll()).thenReturn(List.of(event1));

        try (var mockedStatic = mockStatic(EventServiceImpl.class)) {
            var response = controller.getRecentEvents(null, 5);

            assertTrue(response.getStatusCode().is2xxSuccessful());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().size());
        }
    }

    @Test
    void getRecentEvents_whenException_shouldReturn500() {
        when(eventRepository.findAll()).thenThrow(new RuntimeException("error"));

        var response = controller.getRecentEvents(null, 5);

        assertTrue(response.getStatusCode().is5xxServerError());
    }
}
