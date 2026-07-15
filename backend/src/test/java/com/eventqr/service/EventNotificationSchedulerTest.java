package com.eventqr.service;

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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventNotificationSchedulerTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventTicketRepository eventTicketRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventNotificationScheduler scheduler;

    private Event createEvent(Long eventId, String title, LocalDateTime startTime) {
        Event event = new Event();
        event.setEventId(eventId);
        event.setTitle(title);
        event.setStartTime(startTime);
        return event;
    }

    private EventTicket createTicket(Long eventId, Long userId, boolean cancelled) {
        EventTicket ticket = new EventTicket();
        ticket.setEventId(eventId);
        ticket.setUserId(userId);
        ticket.setCancelled(cancelled);
        return ticket;
    }

    @Test
    void notifyUsersAboutEventsStartingSoon_eventsFound() {
        Event event1 = createEvent(1L, "Event 1", LocalDateTime.now().plusMinutes(30));
        Event event2 = createEvent(2L, "Event 2", LocalDateTime.now().plusMinutes(45));
        when(eventRepository.findEventsStartingSoon(any(), any())).thenReturn(List.of(event1, event2));
        when(eventTicketRepository.findByEventId(1L)).thenReturn(List.of(
                createTicket(1L, 101L, false),
                createTicket(1L, 102L, false)
        ));
        when(eventTicketRepository.findByEventId(2L)).thenReturn(List.of(
                createTicket(2L, 201L, false)
        ));

        scheduler.notifyUsersAboutEventsStartingSoon();

        verify(notificationService, times(3)).sendEventStartSoonNotification(any(), any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsStartingSoon_noEvents() {
        when(eventRepository.findEventsStartingSoon(any(), any())).thenReturn(List.of());

        scheduler.notifyUsersAboutEventsStartingSoon();

        verify(notificationService, never()).sendEventStartSoonNotification(any(), any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsStartingSoon_allTicketsCancelled() {
        Event event = createEvent(1L, "Event", LocalDateTime.now().plusMinutes(30));
        when(eventRepository.findEventsStartingSoon(any(), any())).thenReturn(List.of(event));
        when(eventTicketRepository.findByEventId(1L)).thenReturn(List.of(
                createTicket(1L, 101L, true)
        ));

        scheduler.notifyUsersAboutEventsStartingSoon();

        verify(notificationService, never()).sendEventStartSoonNotification(any(), any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsStartingSoon_notificationException() {
        Event event = createEvent(1L, "Event", LocalDateTime.now().plusMinutes(30));
        when(eventRepository.findEventsStartingSoon(any(), any())).thenReturn(List.of(event));
        when(eventTicketRepository.findByEventId(1L)).thenReturn(List.of(
                createTicket(1L, 101L, false),
                createTicket(1L, 102L, false)
        ));
        doThrow(new RuntimeException("fail"))
                .doNothing()
                .when(notificationService).sendEventStartSoonNotification(any(), any(), any(), any());

        scheduler.notifyUsersAboutEventsStartingSoon();

        verify(notificationService, times(2)).sendEventStartSoonNotification(any(), any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsStartingSoon_perEventException() {
        Event event1 = createEvent(1L, "Event 1", LocalDateTime.now().plusMinutes(30));
        Event event2 = createEvent(2L, "Event 2", LocalDateTime.now().plusMinutes(45));
        when(eventRepository.findEventsStartingSoon(any(), any())).thenReturn(List.of(event1, event2));
        when(eventTicketRepository.findByEventId(1L)).thenThrow(new RuntimeException("DB error"));
        when(eventTicketRepository.findByEventId(2L)).thenReturn(List.of(
                createTicket(2L, 201L, false)
        ));

        scheduler.notifyUsersAboutEventsStartingSoon();

        verify(notificationService, times(1)).sendEventStartSoonNotification(any(), any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsStartingSoon_topLevelException() {
        when(eventRepository.findEventsStartingSoon(any(), any())).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> scheduler.notifyUsersAboutEventsStartingSoon());

        verify(notificationService, never()).sendEventStartSoonNotification(any(), any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsEnded_eventsFound() {
        Event event = createEvent(1L, "Event", LocalDateTime.now());
        when(eventRepository.findEventsJustEnded(any(), any())).thenReturn(List.of(event));
        when(eventTicketRepository.findByEventId(1L)).thenReturn(List.of(
                createTicket(1L, 101L, false),
                createTicket(1L, 102L, false)
        ));

        scheduler.notifyUsersAboutEventsEnded();

        verify(notificationService, times(2)).sendEventEndedNotification(any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsEnded_noEvents() {
        when(eventRepository.findEventsJustEnded(any(), any())).thenReturn(List.of());

        scheduler.notifyUsersAboutEventsEnded();

        verify(notificationService, never()).sendEventEndedNotification(any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsEnded_allTicketsCancelled() {
        Event event = createEvent(1L, "Event", LocalDateTime.now());
        when(eventRepository.findEventsJustEnded(any(), any())).thenReturn(List.of(event));
        when(eventTicketRepository.findByEventId(1L)).thenReturn(List.of(
                createTicket(1L, 101L, true)
        ));

        scheduler.notifyUsersAboutEventsEnded();

        verify(notificationService, never()).sendEventEndedNotification(any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsEnded_notificationException() {
        Event event = createEvent(1L, "Event", LocalDateTime.now());
        when(eventRepository.findEventsJustEnded(any(), any())).thenReturn(List.of(event));
        when(eventTicketRepository.findByEventId(1L)).thenReturn(List.of(
                createTicket(1L, 101L, false),
                createTicket(1L, 102L, false)
        ));
        doThrow(new RuntimeException("fail"))
                .doNothing()
                .when(notificationService).sendEventEndedNotification(any(), any(), any());

        scheduler.notifyUsersAboutEventsEnded();

        verify(notificationService, times(2)).sendEventEndedNotification(any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsEnded_perEventException() {
        Event event1 = createEvent(1L, "Event 1", LocalDateTime.now());
        Event event2 = createEvent(2L, "Event 2", LocalDateTime.now());
        when(eventRepository.findEventsJustEnded(any(), any())).thenReturn(List.of(event1, event2));
        when(eventTicketRepository.findByEventId(1L)).thenThrow(new RuntimeException("DB error"));
        when(eventTicketRepository.findByEventId(2L)).thenReturn(List.of(
                createTicket(2L, 201L, false)
        ));

        scheduler.notifyUsersAboutEventsEnded();

        verify(notificationService, times(1)).sendEventEndedNotification(any(), any(), any());
    }

    @Test
    void notifyUsersAboutEventsEnded_topLevelException() {
        when(eventRepository.findEventsJustEnded(any(), any())).thenThrow(new RuntimeException("DB error"));

        assertDoesNotThrow(() -> scheduler.notifyUsersAboutEventsEnded());

        verify(notificationService, never()).sendEventEndedNotification(any(), any(), any());
    }
}
