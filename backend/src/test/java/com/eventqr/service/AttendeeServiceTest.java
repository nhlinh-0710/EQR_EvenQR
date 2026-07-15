package com.eventqr.service;

import com.eventqr.dto.AttendeeResponse;
import com.eventqr.dto.AttendeeStatistics;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.repository.CheckinRepository;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendeeServiceTest {

    @Mock
    private EventTicketRepository ticketRepository;

    @Mock
    private CheckinRepository checkinRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private AttendeeService attendeeService;

    private Event testEvent;
    private EventTicket ticket1;
    private EventTicket ticket2;
    private EventTicket cancelledTicket;
    private final Long eventId = 100L;
    private final Long organizerId = 10L;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setEventId(eventId);
        testEvent.setOrganizerId(organizerId);
        testEvent.setTitle("Test Event");
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        testEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(3));

        ticket1 = new EventTicket();
        ticket1.setTicketId(1L);
        ticket1.setEventId(eventId);
        ticket1.setUserId(101L);
        ticket1.setName("User One");
        ticket1.setEmail("user1@example.com");
        ticket1.setPhone("0111111111");
        ticket1.setOccupation("Engineer");
        ticket1.setTicketType("VIP");
        ticket1.setRegisteredAt(LocalDateTime.now());
        ticket1.setCancelled(false);

        ticket2 = new EventTicket();
        ticket2.setTicketId(2L);
        ticket2.setEventId(eventId);
        ticket2.setUserId(102L);
        ticket2.setName("User Two");
        ticket2.setEmail("user2@example.com");
        ticket2.setPhone("0222222222");
        ticket2.setOccupation("Designer");
        ticket2.setTicketType("Standard");
        ticket2.setRegisteredAt(LocalDateTime.now());
        ticket2.setCancelled(false);

        cancelledTicket = new EventTicket();
        cancelledTicket.setTicketId(3L);
        cancelledTicket.setEventId(eventId);
        cancelledTicket.setUserId(103L);
        cancelledTicket.setName("User Three");
        cancelledTicket.setEmail("user3@example.com");
        cancelledTicket.setPhone("0333333333");
        cancelledTicket.setOccupation("Manager");
        cancelledTicket.setTicketType("Standard");
        cancelledTicket.setRegisteredAt(LocalDateTime.now());
        cancelledTicket.setCancelled(true);
    }

    @Test
    void getAttendeesByEventId_whenEventNotFound_shouldThrowException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> attendeeService.getAttendeesByEventId(eventId, organizerId));
    }

    @Test
    void getAttendeesByEventId_whenNotOwner_shouldThrowException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        assertThrows(IllegalStateException.class,
                () -> attendeeService.getAttendeesByEventId(eventId, 999L));
    }

    @Test
    void getAttendeesByEventId_shouldMapStatusCorrectly() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEventId(eventId)).thenReturn(List.of(ticket1, ticket2, cancelledTicket));
        when(checkinRepository.existsByEventIdAndUserId(eventId, 101L)).thenReturn(true);
        when(checkinRepository.existsByEventIdAndUserId(eventId, 102L)).thenReturn(false);

        List<AttendeeResponse> result = attendeeService.getAttendeesByEventId(eventId, organizerId);

        assertEquals(3, result.size());
        assertEquals("CHECKED_IN", result.get(0).getStatus());
        assertEquals("REGISTERED", result.get(1).getStatus());
        assertEquals("CANCELLED", result.get(2).getStatus());
    }

    @Test
    void getAttendeeStatistics_shouldComputeCorrectly() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(ticketRepository.findByEventId(eventId)).thenReturn(List.of(ticket1, ticket2, cancelledTicket));
        when(checkinRepository.countByEventId(eventId)).thenReturn(1);

        AttendeeStatistics stats = attendeeService.getAttendeeStatistics(eventId, organizerId);

        assertEquals(eventId, stats.getEventId());
        assertEquals("Test Event", stats.getEventTitle());
        assertEquals(2, stats.getTotalRegistered());
        assertEquals(1, stats.getTotalCheckedIn());
        assertEquals(1, stats.getTotalPending());
        assertEquals(1, stats.getTotalCancelled());
        assertEquals(50.0, stats.getCheckInRate(), 0.001);
    }

    @Test
    void getAttendeeStatistics_whenNotOwner_shouldThrowException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));

        assertThrows(IllegalStateException.class,
                () -> attendeeService.getAttendeeStatistics(eventId, 999L));
    }

    @Test
    void getAllEventsStatistics_shouldAggregateAcrossEvents() {
        Event event2 = new Event();
        event2.setEventId(200L);
        event2.setOrganizerId(organizerId);
        event2.setTitle("Event 2");

        EventTicket event2Ticket = new EventTicket();
        event2Ticket.setTicketId(4L);
        event2Ticket.setEventId(200L);
        event2Ticket.setUserId(201L);
        event2Ticket.setCancelled(false);

        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
                .thenReturn(List.of(testEvent, event2));
        when(ticketRepository.findByEventId(eventId)).thenReturn(List.of(ticket1, ticket2, cancelledTicket));
        when(ticketRepository.findByEventId(200L)).thenReturn(List.of(event2Ticket));
        when(checkinRepository.countByEventId(eventId)).thenReturn(1);
        when(checkinRepository.countByEventId(200L)).thenReturn(0);

        AttendeeStatistics stats = attendeeService.getAllEventsStatistics(organizerId);

        assertNull(stats.getEventId());
        assertEquals("Tất cả sự kiện", stats.getEventTitle());
        assertEquals(3, stats.getTotalRegistered());
        assertEquals(1, stats.getTotalCheckedIn());
        assertEquals(2, stats.getTotalPending());
        assertEquals(1, stats.getTotalCancelled());
    }
}
