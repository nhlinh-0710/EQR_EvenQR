package com.eventqr.service;

import com.eventqr.dto.EventStatisticsDTO;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventTicketRepository eventTicketRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getOrganizerStatistics_whenNoEvents_shouldReturnZeroCounts() {
        Long organizerId = 1L;
        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId)).thenReturn(List.of());

        EventStatisticsDTO result = statisticsService.getOrganizerStatistics(organizerId);

        assertEquals(organizerId, result.getOrganizerId());
        assertEquals(0, result.getTotalEvents());
        assertEquals(0, result.getTotalRegistrations());
        assertNull(result.getEventStats());
        assertNull(result.getTimeSeriesStats());
    }

    @Test
    void getOrganizerStatistics_whenEventsWithTickets_shouldReturnCorrectCounts() {
        Long organizerId = 1L;
        Event event1 = new Event();
        event1.setEventId(10L);
        event1.setTitle("Event One");
        event1.setStatus("active");
        event1.setStartTime(LocalDateTime.of(2026, 3, 15, 10, 0));
        event1.setOrganizerId(organizerId);

        Event event2 = new Event();
        event2.setEventId(20L);
        event2.setTitle("Event Two");
        event2.setStatus("completed");
        event2.setStartTime(LocalDateTime.of(2026, 5, 20, 14, 0));
        event2.setOrganizerId(organizerId);

        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
            .thenReturn(List.of(event1, event2));

        EventTicket ticket1 = new EventTicket();
        ticket1.setEventId(10L);
        ticket1.setCancelled(false);
        ticket1.setRegisteredAt(LocalDateTime.now());

        EventTicket ticket2 = new EventTicket();
        ticket2.setEventId(10L);
        ticket2.setCancelled(false);
        ticket2.setRegisteredAt(LocalDateTime.now());

        EventTicket cancelledTicket = new EventTicket();
        cancelledTicket.setEventId(10L);
        cancelledTicket.setCancelled(true);
        cancelledTicket.setRegisteredAt(LocalDateTime.now());

        EventTicket ticket3 = new EventTicket();
        ticket3.setEventId(20L);
        ticket3.setCancelled(false);
        ticket3.setRegisteredAt(LocalDateTime.now());

        when(eventTicketRepository.findByEventId(10L)).thenReturn(List.of(ticket1, ticket2, cancelledTicket));
        when(eventTicketRepository.findByEventId(20L)).thenReturn(List.of(ticket3));

        EventStatisticsDTO result = statisticsService.getOrganizerStatistics(organizerId);

        assertEquals(2, result.getTotalEvents());
        assertEquals(3, result.getTotalRegistrations());
    }

    @Test
    void getOrganizerStatistics_whenEventHasNullStartTime_shouldSetUnknownDate() {
        Long organizerId = 1L;
        Event event = new Event();
        event.setEventId(10L);
        event.setTitle("No Date Event");
        event.setStatus("active");
        event.setStartTime(null);
        event.setOrganizerId(organizerId);

        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
            .thenReturn(List.of(event));

        when(eventTicketRepository.findByEventId(10L)).thenReturn(List.of());

        EventStatisticsDTO result = statisticsService.getOrganizerStatistics(organizerId);

        assertEquals(1, result.getTotalEvents());
        assertNotNull(result.getEventStats());
        assertEquals(1, result.getEventStats().size());
        assertEquals("Chưa xác định", result.getEventStats().get(0).getEventDate());
    }

    @Test
    void getOrganizerStatistics_timeSeries_shouldIncludeOnlyUncancelledTicketsIn6Months() {
        Long organizerId = 1L;
        Event event = new Event();
        event.setEventId(10L);
        event.setTitle("Time Series Event");
        event.setStatus("active");
        event.setStartTime(LocalDateTime.now());
        event.setOrganizerId(organizerId);

        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
            .thenReturn(List.of(event));

        LocalDateTime now = LocalDateTime.now();

        EventTicket validTicket = new EventTicket();
        validTicket.setEventId(10L);
        validTicket.setCancelled(false);
        validTicket.setRegisteredAt(now.minusMonths(2));

        EventTicket cancelledTicket = new EventTicket();
        cancelledTicket.setEventId(10L);
        cancelledTicket.setCancelled(true);
        cancelledTicket.setRegisteredAt(now.minusMonths(3));

        EventTicket outsideRangeTicket = new EventTicket();
        outsideRangeTicket.setEventId(10L);
        outsideRangeTicket.setCancelled(false);
        outsideRangeTicket.setRegisteredAt(now.minusMonths(12));

        when(eventTicketRepository.findByEventId(10L))
            .thenReturn(List.of(validTicket, cancelledTicket, outsideRangeTicket));

        EventStatisticsDTO result = statisticsService.getOrganizerStatistics(organizerId);

        assertNotNull(result.getTimeSeriesStats());
        assertEquals(6, result.getTimeSeriesStats().size());

        int totalRegisteredInSeries = result.getTimeSeriesStats().stream()
            .mapToInt(EventStatisticsDTO.TimeSeriesStat::getRegistrationCount)
            .sum();

        assertEquals(1, totalRegisteredInSeries);
    }

    @Test
    void getOrganizerStatistics_eventStats_shouldBePopulatedCorrectly() {
        Long organizerId = 1L;
        Event event = new Event();
        event.setEventId(10L);
        event.setTitle("My Event");
        event.setStatus("active");
        event.setStartTime(LocalDateTime.of(2026, 7, 10, 9, 0));
        event.setOrganizerId(organizerId);

        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
            .thenReturn(List.of(event));

        EventTicket t1 = new EventTicket();
        t1.setEventId(10L);
        t1.setCancelled(false);

        EventTicket t2 = new EventTicket();
        t2.setEventId(10L);
        t2.setCancelled(false);

        EventTicket t3 = new EventTicket();
        t3.setEventId(10L);
        t3.setCancelled(true);

        when(eventTicketRepository.findByEventId(10L)).thenReturn(List.of(t1, t2, t3));

        EventStatisticsDTO result = statisticsService.getOrganizerStatistics(organizerId);

        assertNotNull(result.getEventStats());
        assertEquals(1, result.getEventStats().size());

        EventStatisticsDTO.EventRegistrationStat stat = result.getEventStats().get(0);
        assertEquals(10L, stat.getEventId());
        assertEquals("My Event", stat.getEventTitle());
        assertEquals(2, stat.getRegistrationCount());
        assertEquals("active", stat.getEventStatus());
        assertEquals("10/07/2026", stat.getEventDate());
    }
}
