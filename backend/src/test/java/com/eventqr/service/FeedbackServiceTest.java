package com.eventqr.service;

import com.eventqr.dto.*;
import com.eventqr.model.Account;
import com.eventqr.model.Event;
import com.eventqr.model.EventTicket;
import com.eventqr.model.Feedback;
import com.eventqr.repository.*;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventTicketRepository eventTicketRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CheckinRepository checkinRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private FeedbackService feedbackService;

    @Captor
    private ArgumentCaptor<Feedback> feedbackCaptor;

    private Event completedEvent;
    private Event ongoingEvent;
    private Account testUser;
    private Account testOrganizer;
    private FeedbackRequest validRequest;
    private EventTicket testTicket;
    private final Long userId = 1L;
    private final Long organizerId = 10L;
    private final Long eventId = 100L;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        completedEvent = new Event();
        completedEvent.setEventId(eventId);
        completedEvent.setOrganizerId(organizerId);
        completedEvent.setTitle("Completed Event");
        completedEvent.setStartTime(now.minusDays(2));
        completedEvent.setEndTime(now.minusDays(1));
        completedEvent.setStatus("COMPLETED");

        ongoingEvent = new Event();
        ongoingEvent.setEventId(200L);
        ongoingEvent.setOrganizerId(organizerId);
        ongoingEvent.setTitle("Ongoing Event");
        ongoingEvent.setStartTime(now.minusHours(1));
        ongoingEvent.setEndTime(now.plusHours(2));
        ongoingEvent.setStatus("ONGOING");

        testUser = new Account("Test User", "user@example.com", "pass", "0123456789", "user");
        testUser.setId(userId);

        testOrganizer = new Account("Organizer", "org@example.com", "pass", "0987654321", "organizer");
        testOrganizer.setId(organizerId);

        validRequest = new FeedbackRequest(eventId, userId, 5, "Great event!");

        testTicket = new EventTicket();
        testTicket.setTicketId(1L);
        testTicket.setEventId(eventId);
        testTicket.setUserId(userId);
        testTicket.setCancelled(false);
    }

    @Test
    void submitFeedback_whenNullEventId_shouldThrowException() {
        FeedbackRequest req = new FeedbackRequest(null, userId, 5, "Bad");

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.submitFeedback(req));
    }

    @Test
    void submitFeedback_whenNullUserId_shouldThrowException() {
        FeedbackRequest req = new FeedbackRequest(eventId, null, 5, "Bad");

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.submitFeedback(req));
    }

    @Test
    void submitFeedback_whenRatingOutOfRange_shouldThrowException() {
        FeedbackRequest low = new FeedbackRequest(eventId, userId, 0, "Bad");
        FeedbackRequest high = new FeedbackRequest(eventId, userId, 6, "Bad");

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.submitFeedback(low));
        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.submitFeedback(high));
    }

    @Test
    void submitFeedback_whenEventNotEnded_shouldThrowException() {
        when(eventRepository.findById(200L)).thenReturn(Optional.of(ongoingEvent));

        FeedbackRequest req = new FeedbackRequest(200L, userId, 4, "Good");

        assertThrows(IllegalStateException.class,
                () -> feedbackService.submitFeedback(req));
    }

    @Test
    void submitFeedback_whenOrganizerFeedbackOnOwnEvent_shouldThrowException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));

        FeedbackRequest req = new FeedbackRequest(eventId, organizerId, 5, "Great");

        assertThrows(IllegalStateException.class,
                () -> feedbackService.submitFeedback(req));
    }

    @Test
    void submitFeedback_whenUserNotParticipated_shouldThrowException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(eventTicketRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);
        when(checkinRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);

        assertThrows(IllegalStateException.class,
                () -> feedbackService.submitFeedback(validRequest));
    }

    @Test
    void submitFeedback_whenValidNewFeedback_shouldSaveAndNotify() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(eventTicketRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);
        when(feedbackRepository.findByEventIdAndUserId(eventId, userId)).thenReturn(Optional.empty());
        when(accountRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback f = invocation.getArgument(0);
            f.setFeedbackId(999L);
            return f;
        });

        FeedbackResponse result = feedbackService.submitFeedback(validRequest);

        assertNotNull(result);
        assertEquals(999L, result.getFeedbackId());
        assertEquals(5, result.getRating());
        assertEquals("Great event!", result.getComment());
        assertEquals("Test User", result.getUserName());

        verify(notificationService).sendFeedbackNotificationToOrganizer(
                organizerId, eventId, "Completed Event", "Test User", 5);
    }

    @Test
    void submitFeedback_whenUpdatingWithin24h_shouldUpdateExisting() {
        Feedback existingFeedback = new Feedback(eventId, userId, 3, "OK");
        existingFeedback.setFeedbackId(10L);
        existingFeedback.setCreatedAt(LocalDateTime.now().minusHours(2));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(eventTicketRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);
        when(feedbackRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.of(existingFeedback));
        when(accountRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FeedbackResponse result = feedbackService.submitFeedback(validRequest);

        assertEquals(5, result.getRating());
        assertEquals("Great event!", result.getComment());
        assertEquals(10L, result.getFeedbackId());
        verify(notificationService, never()).sendFeedbackNotificationToOrganizer(
                anyLong(), anyLong(), anyString(), anyString(), anyInt());
    }

    @Test
    void submitFeedback_whenUpdatingAfter24h_shouldThrowException() {
        Feedback existingFeedback = new Feedback(eventId, userId, 3, "OK");
        existingFeedback.setCreatedAt(LocalDateTime.now().minusDays(2));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(eventTicketRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);
        when(feedbackRepository.findByEventIdAndUserId(eventId, userId))
                .thenReturn(Optional.of(existingFeedback));

        assertThrows(IllegalStateException.class,
                () -> feedbackService.submitFeedback(validRequest));
    }

    @Test
    void replyFeedback_whenFeedbackNotFound_shouldThrowException() {
        when(feedbackRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.replyFeedback(1L, organizerId, "Thanks!"));
    }

    @Test
    void replyFeedback_whenNotOwner_shouldThrowException() {
        Feedback feedback = new Feedback(eventId, userId, 5, "Good");
        feedback.setFeedbackId(1L);

        Event otherEvent = new Event();
        otherEvent.setEventId(eventId);
        otherEvent.setOrganizerId(999L);

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(otherEvent));

        assertThrows(IllegalStateException.class,
                () -> feedbackService.replyFeedback(1L, organizerId, "Thanks!"));
    }

    @Test
    void replyFeedback_whenReplyEmpty_shouldThrowException() {
        Feedback feedback = new Feedback(eventId, userId, 5, "Good");
        feedback.setFeedbackId(1L);

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));

        assertThrows(IllegalArgumentException.class,
                () -> feedbackService.replyFeedback(1L, organizerId, "   "));
    }

    @Test
    void replyFeedback_whenValid_shouldSaveReplyAndNotify() {
        Feedback feedback = new Feedback(eventId, userId, 4, "Nice event");
        feedback.setFeedbackId(1L);
        feedback.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(feedbackRepository.findById(1L)).thenReturn(Optional.of(feedback));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(accountRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(feedbackRepository.save(any(Feedback.class))).thenAnswer(invocation -> {
            Feedback f = invocation.getArgument(0);
            f.setOrganizerReplyAt(LocalDateTime.now());
            return f;
        });

        FeedbackResponse result = feedbackService.replyFeedback(1L, organizerId, "Thank you!");

        assertNotNull(result);
        assertEquals("Thank you!", result.getOrganizerReply());
        assertNotNull(result.getOrganizerReplyAt());

        verify(notificationService).sendFeedbackReplyNotificationToUser(
                userId, eventId, "Completed Event", "Thank you!");
    }

    @Test
    void getCompletedEventsForUser_shouldReturnOnlyEndedEvents() {
        EventTicket ticket2 = new EventTicket();
        ticket2.setTicketId(2L);
        ticket2.setEventId(200L);
        ticket2.setUserId(userId);
        ticket2.setCancelled(false);

        when(eventTicketRepository.findByUserId(userId)).thenReturn(List.of(testTicket, ticket2));
        when(eventRepository.findAllById(List.of(eventId, 200L))).thenReturn(List.of(completedEvent, ongoingEvent));
        when(feedbackRepository.findEventIdsByUserId(userId)).thenReturn(List.of());

        List<CompletedEventResponse> result = feedbackService.getCompletedEventsForUser(userId);

        assertEquals(1, result.size());
        assertEquals("Completed Event", result.get(0).getTitle());
        assertFalse(result.get(0).getHasFeedback());
    }

    @Test
    void getCompletedEventsForUser_whenUserHasNoTickets_shouldReturnEmpty() {
        when(eventTicketRepository.findByUserId(userId)).thenReturn(List.of());

        List<CompletedEventResponse> result = feedbackService.getCompletedEventsForUser(userId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCompletedEventsForUser_shouldCheckFeedbackStatus() {
        when(eventTicketRepository.findByUserId(userId)).thenReturn(List.of(testTicket));
        when(eventRepository.findAllById(List.of(eventId))).thenReturn(List.of(completedEvent));
        when(feedbackRepository.findEventIdsByUserId(userId)).thenReturn(List.of(eventId));

        List<CompletedEventResponse> result = feedbackService.getCompletedEventsForUser(userId);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getHasFeedback());
    }

    @Test
    void getFeedbacksByEventId_shouldReturnMappedResults() {
        Feedback feedback = new Feedback(eventId, userId, 4, "Good event");
        feedback.setFeedbackId(1L);
        feedback.setCreatedAt(LocalDateTime.now());

        when(feedbackRepository.findByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(List.of(feedback));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(accountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        List<FeedbackResponse> result = feedbackService.getFeedbacksByEventId(eventId);

        assertEquals(1, result.size());
        assertEquals(4, result.get(0).getRating());
        assertEquals("Test User", result.get(0).getUserName());
    }

    @Test
    void getFeedbacksByUserId_shouldReturnUserFeedbacks() {
        Feedback feedback = new Feedback(eventId, userId, 5, "Perfect");
        feedback.setFeedbackId(1L);

        when(feedbackRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(feedback));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(accountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        List<FeedbackResponse> result = feedbackService.getFeedbacksByUserId(userId);

        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getRating());
    }

    @Test
    void getOrganizerFeedbacks_shouldReturnFormattedResponses() {
        Feedback feedback = new Feedback(eventId, userId, 4, "Good");
        feedback.setFeedbackId(1L);
        feedback.setCreatedAt(LocalDateTime.of(2025, 6, 15, 10, 30));

        when(feedbackRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
                .thenReturn(List.of(feedback));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(completedEvent));
        when(accountRepository.findById(userId)).thenReturn(Optional.of(testUser));

        List<OrganizerFeedbackResponse> result = feedbackService.getOrganizerFeedbacks(organizerId);

        assertEquals(1, result.size());
        assertEquals("Completed Event", result.get(0).getEventName());
        assertEquals("Test User", result.get(0).getUserName());
        assertEquals(4, result.get(0).getRating());
    }

    @Test
    void getOrganizerFeedbacks_whenNoFeedbacks_shouldReturnEmpty() {
        when(feedbackRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
                .thenReturn(List.of());

        List<OrganizerFeedbackResponse> result = feedbackService.getOrganizerFeedbacks(organizerId);

        assertTrue(result.isEmpty());
    }

    @Test
    void getFeedbacksByOrganizerId_whenNoEvents_shouldReturnEmpty() {
        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
                .thenReturn(List.of());

        List<FeedbackResponse> result = feedbackService.getFeedbacksByOrganizerId(organizerId);

        assertTrue(result.isEmpty());
    }
}
