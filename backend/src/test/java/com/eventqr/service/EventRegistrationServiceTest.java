package com.eventqr.service;

import com.eventqr.dto.EventRegisterRequest;
import com.eventqr.dto.UserTicketResponse;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.repository.EventRepository;
import com.eventqr.repository.EventTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
class EventRegistrationServiceTest {

    @Mock
    private EventTicketRepository eventTicketRepo;

    @Mock
    private EventRepository eventRepo;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventRegistrationService registrationService;

    @Captor
    private ArgumentCaptor<EventTicket> ticketCaptor;

    private EventRegisterRequest validRequest;
    private Event testEvent;
    private EventTicket testTicket;
    private final Long userId = 1L;
    private final Long eventId = 100L;
    private final Long organizerId = 10L;

    @BeforeEach
    void setUp() {
        validRequest = new EventRegisterRequest();
        validRequest.setEventId(eventId);
        validRequest.setUserId(userId);
        validRequest.setName("Nguyen Van A");
        validRequest.setEmail("user@example.com");
        validRequest.setPhone("0123456789");
        validRequest.setOccupation("Engineer");
        validRequest.setTicketType("VIP");
        validRequest.setNote("No preference");

        testEvent = new Event();
        testEvent.setEventId(eventId);
        testEvent.setOrganizerId(organizerId);
        testEvent.setTitle("Test Event");
        testEvent.setStartTime(LocalDateTime.now().plusDays(1));
        testEvent.setEndTime(LocalDateTime.now().plusDays(1).plusHours(3));

        testTicket = new EventTicket();
        testTicket.setTicketId(1L);
        testTicket.setEventId(eventId);
        testTicket.setUserId(userId);
        testTicket.setName("Nguyen Van A");
        testTicket.setEmail("user@example.com");
        testTicket.setPhone("0123456789");
        testTicket.setOccupation("Engineer");
        testTicket.setTicketType("VIP");
        testTicket.setNote("No preference");
        testTicket.setRegisteredAt(LocalDateTime.now());
        testTicket.setCancelled(false);
    }

    @Test
    void register_whenAlreadyRegistered_shouldThrowException() {
        when(eventTicketRepo.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> registrationService.register(validRequest));
        verify(eventTicketRepo, never()).save(any());
    }

    @Test
    void register_whenEventNotFound_shouldThrowException() {
        when(eventTicketRepo.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(eventRepo.findById(eventId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> registrationService.register(validRequest));
    }

    @Test
    void register_whenValid_shouldSaveTicketAndNotify() {
        when(eventTicketRepo.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventTicketRepo.save(any(EventTicket.class))).thenReturn(testTicket);

        EventTicket result = registrationService.register(validRequest);

        assertNotNull(result);
        assertEquals(eventId, result.getEventId());
        assertEquals(userId, result.getUserId());
        assertEquals("VIP", result.getTicketType());

        verify(notificationService).sendToOrganizer(
                organizerId, eventId, "Test Event", "Nguyen Van A");
    }

    @Test
    void register_whenNotificationFails_shouldNotThrow() {
        when(eventTicketRepo.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(eventRepo.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventTicketRepo.save(any(EventTicket.class))).thenReturn(testTicket);
        doThrow(new RuntimeException("WS error")).when(notificationService)
                .sendToOrganizer(anyLong(), anyLong(), anyString(), anyString());

        assertDoesNotThrow(() -> registrationService.register(validRequest));
    }

    @Test
    void getTicketsOfUser_shouldReturnMappedResponses() {
        Event event2 = new Event();
        event2.setEventId(200L);
        event2.setTitle("Event 2");
        event2.setLocation("Hall B");
        event2.setStartTime(LocalDateTime.now().plusDays(2));
        event2.setEndTime(LocalDateTime.now().plusDays(2).plusHours(3));
        event2.setStatus("UPCOMING");

        EventTicket ticket2 = new EventTicket();
        ticket2.setTicketId(2L);
        ticket2.setEventId(200L);
        ticket2.setUserId(userId);
        ticket2.setTicketType("Standard");
        ticket2.setRegisteredAt(LocalDateTime.now());
        ticket2.setCancelled(false);

        when(eventTicketRepo.findByUserId(userId)).thenReturn(List.of(testTicket, ticket2));
        when(eventRepo.findAllById(List.of(eventId, 200L))).thenReturn(List.of(testEvent, event2));

        List<UserTicketResponse> result = registrationService.getTicketsOfUser(userId);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getTicketId());
        assertEquals("Test Event", result.get(0).getEventTitle());
        assertFalse(result.get(0).getCancelled());
    }

    @Test
    void getTicketsOfUser_whenTicketCancelled_shouldReflectInResponse() {
        testTicket.setCancelled(true);
        when(eventTicketRepo.findByUserId(userId)).thenReturn(List.of(testTicket));
        when(eventRepo.findAllById(List.of(eventId))).thenReturn(List.of(testEvent));

        List<UserTicketResponse> result = registrationService.getTicketsOfUser(userId);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getCancelled());
    }

    @Test
    void getTicketsOfUser_withUnknownEvent_shouldSkip() {
        testTicket.setEventId(999L);
        when(eventTicketRepo.findByUserId(userId)).thenReturn(List.of(testTicket));
        when(eventRepo.findAllById(List.of(999L))).thenReturn(List.of());

        List<UserTicketResponse> result = registrationService.getTicketsOfUser(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void cancelTicket_whenTicketNotFound_shouldThrowException() {
        when(eventTicketRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> registrationService.cancelTicket(userId, 1L));
    }

    @Test
    void cancelTicket_whenNotOwner_shouldThrowException() {
        when(eventTicketRepo.findById(1L)).thenReturn(Optional.of(testTicket));

        assertThrows(IllegalStateException.class,
                () -> registrationService.cancelTicket(999L, 1L));
    }

    @Test
    void cancelTicket_whenValid_shouldSetCancelled() {
        when(eventTicketRepo.findById(1L)).thenReturn(Optional.of(testTicket));
        when(eventTicketRepo.save(any(EventTicket.class))).thenReturn(testTicket);

        registrationService.cancelTicket(userId, 1L);

        verify(eventTicketRepo).save(ticketCaptor.capture());
        assertTrue(ticketCaptor.getValue().getCancelled());
    }
}
