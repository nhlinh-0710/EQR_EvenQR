package com.eventqr.service;

import com.eventqr.dto.EventRequest;
import com.eventqr.model.Event;
import com.eventqr.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventRequest upcomingRequest;
    private EventRequest ongoingRequest;
    private EventRequest completedRequest;
    private Event existingEvent;
    private final Long organizerId = 1L;
    private final Long eventId = 100L;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        upcomingRequest = new EventRequest();
        upcomingRequest.setTitle("Future Event");
        upcomingRequest.setDescription("A future event");
        upcomingRequest.setLocation("Hall A");
        upcomingRequest.setCategory("Conference");
        upcomingRequest.setEventDate(now.plusDays(7));
        upcomingRequest.setDuration(3);
        upcomingRequest.setMaxParticipants(100);
        upcomingRequest.setOrganizerId(organizerId);

        ongoingRequest = new EventRequest();
        ongoingRequest.setTitle("Ongoing Event");
        ongoingRequest.setDescription("Happening now");
        ongoingRequest.setLocation("Hall B");
        ongoingRequest.setCategory("Workshop");
        ongoingRequest.setEventDate(now.minusHours(1));
        ongoingRequest.setDuration(4);
        ongoingRequest.setMaxParticipants(50);
        ongoingRequest.setOrganizerId(organizerId);

        completedRequest = new EventRequest();
        completedRequest.setTitle("Past Event");
        completedRequest.setDescription("Already finished");
        completedRequest.setLocation("Hall C");
        completedRequest.setCategory("Seminar");
        completedRequest.setEventDate(now.minusDays(2));
        completedRequest.setDuration(2);
        completedRequest.setMaxParticipants(30);
        completedRequest.setOrganizerId(organizerId);

        existingEvent = new Event();
        existingEvent.setEventId(eventId);
        existingEvent.setOrganizerId(organizerId);
        existingEvent.setTitle("Original Title");
        existingEvent.setDescription("Original Desc");
        existingEvent.setLocation("Original Location");
        existingEvent.setCategory("Original Category");
        existingEvent.setStartTime(now.plusDays(1));
        existingEvent.setEndTime(now.plusDays(1).plusHours(2));
        existingEvent.setMaxParticipants(50);
        existingEvent.setImageUrl("old-image.jpg");
        existingEvent.setStatus("UPCOMING");
        existingEvent.setCreatedAt(now);
        existingEvent.setUpdatedAt(now);
    }

    @Test
    void createEvent_withUpcomingDate_shouldSetStatusUpcoming() throws Exception {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(upcomingRequest);

        assertEquals("UPCOMING", result.getStatus());
        assertEquals("Future Event", result.getTitle());
        assertEquals(organizerId, result.getOrganizerId());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void createEvent_withOngoingDate_shouldSetStatusOngoing() throws Exception {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(ongoingRequest);

        assertEquals("ONGOING", result.getStatus());
    }

    @Test
    void createEvent_withPastDate_shouldSetStatusCompleted() throws Exception {
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(completedRequest);

        assertEquals("COMPLETED", result.getStatus());
    }

    @Test
    void createEvent_withExplicitStatus_shouldUseGivenStatus() throws Exception {
        upcomingRequest.setStatus("DRAFT");
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(upcomingRequest);

        assertEquals("DRAFT", result.getStatus());
    }

    @Test
    void createEvent_withNullOrganizerId_shouldThrowException() {
        upcomingRequest.setOrganizerId(null);

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> eventService.createEvent(upcomingRequest));
        assertTrue(ex.getMessage().contains("organizerId"));
    }

    @Test
    void createEvent_withDefaultDuration_shouldUseTwoHours() throws Exception {
        upcomingRequest.setDuration(null);
        LocalDateTime start = upcomingRequest.getEventDate();
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.createEvent(upcomingRequest);

        assertEquals(start.plusHours(2), result.getEndTime());
    }

    @Test
    void getEventById_whenExists_shouldReturnEvent() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        Event result = eventService.getEventById(eventId);

        assertNotNull(result);
        assertEquals("Original Title", result.getTitle());
    }

    @Test
    void getEventById_whenNotExists_shouldThrowException() {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> eventService.getEventById(999L));
    }

    @Test
    void findAll_shouldReturnAllEvents() {
        when(eventRepository.findAll()).thenReturn(List.of(existingEvent));

        List<Event> result = eventService.findAll();

        assertEquals(1, result.size());
    }

    @Test
    void findByOrganizerId_shouldReturnOrganizerEvents() {
        when(eventRepository.findByOrganizerIdOrderByCreatedAtDesc(organizerId))
                .thenReturn(List.of(existingEvent));

        List<Event> result = eventService.findByOrganizerId(organizerId);

        assertEquals(1, result.size());
    }

    @Test
    void isEventOwner_whenOwner_shouldReturnTrue() {
        when(eventRepository.existsByEventIdAndOrganizerId(eventId, organizerId)).thenReturn(true);

        assertTrue(eventService.isEventOwner(eventId, organizerId));
    }

    @Test
    void isEventOwner_whenNotOwner_shouldReturnFalse() {
        when(eventRepository.existsByEventIdAndOrganizerId(eventId, 999L)).thenReturn(false);

        assertFalse(eventService.isEventOwner(eventId, 999L));
    }

    @Test
    void updateEvent_whenNotOwner_shouldThrowException() {
        EventRequest req = new EventRequest();
        req.setTitle("Updated");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        assertThrows(IllegalStateException.class,
                () -> eventService.updateEvent(eventId, req, 999L));
    }

    @Test
    void updateEvent_whenOwner_shouldUpdateFields() throws Exception {
        EventRequest req = new EventRequest();
        req.setTitle("Updated Title");
        req.setDescription("Updated Desc");
        req.setLocation("Updated Location");
        req.setCategory("Updated Category");
        req.setMaxParticipants(200);
        req.setStatus("DRAFT");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(eventId, req, organizerId);

        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Desc", result.getDescription());
        assertEquals("Updated Location", result.getLocation());
        assertEquals("Updated Category", result.getCategory());
        assertEquals(200, result.getMaxParticipants());
        assertEquals("DRAFT", result.getStatus());
    }

    @Test
    void updateEvent_withNewImage_shouldReplaceImage() throws Exception {
        MultipartFile newImage = new MockMultipartFile("image", "new.jpg", "image/jpeg", "data".getBytes());
        EventRequest req = new EventRequest();
        req.setEventImage(newImage);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(fileStorageService.saveFile(newImage)).thenReturn("new-image.jpg");
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Event result = eventService.updateEvent(eventId, req, organizerId);

        verify(fileStorageService).deleteFile("old-image.jpg");
        assertEquals("new-image.jpg", result.getImageUrl());
    }

    @Test
    void deleteEvent_whenNotOwner_shouldThrowException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        assertThrows(IllegalStateException.class,
                () -> eventService.deleteEvent(eventId, 999L));
    }

    @Test
    void deleteEvent_whenOwner_shouldDeleteEventAndImage() throws Exception {
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        doNothing().when(eventRepository).delete(any(Event.class));

        eventService.deleteEvent(eventId, organizerId);

        verify(fileStorageService).deleteFile("old-image.jpg");
        verify(eventRepository).delete(existingEvent);
    }

    @Test
    void deleteEvent_withoutImage_shouldStillDelete() throws Exception {
        existingEvent.setImageUrl(null);
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        doNothing().when(eventRepository).delete(any(Event.class));

        eventService.deleteEvent(eventId, organizerId);

        verify(fileStorageService, never()).deleteFile(any());
        verify(eventRepository).delete(existingEvent);
    }

    @Test
    void updateEventStatus_whenCancelled_shouldNotChange() {
        Event event = new Event();
        event.setStatus("CANCELLED");
        event.setStartTime(LocalDateTime.now().minusDays(1));
        event.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));

        EventServiceImpl.updateEventStatus(event, LocalDateTime.now());

        assertEquals("CANCELLED", event.getStatus());
    }

    @Test
    void updateEventStatus_whenDraft_shouldNotChange() {
        Event event = new Event();
        event.setStatus("DRAFT");
        event.setStartTime(LocalDateTime.now().minusDays(1));
        event.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));

        EventServiceImpl.updateEventStatus(event, LocalDateTime.now());

        assertEquals("DRAFT", event.getStatus());
    }

    @Test
    void updateEventStatus_whenBeforeStart_shouldSetUpcoming() {
        Event event = new Event();
        event.setStatus("UPCOMING");
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));

        EventServiceImpl.updateEventStatus(event, LocalDateTime.now());

        assertEquals("UPCOMING", event.getStatus());
    }

    @Test
    void updateEventStatus_whenAfterEnd_shouldSetCompleted() {
        Event event = new Event();
        event.setStatus("ONGOING");
        event.setStartTime(LocalDateTime.now().minusDays(2));
        event.setEndTime(LocalDateTime.now().minusDays(1));

        EventServiceImpl.updateEventStatus(event, LocalDateTime.now());

        assertEquals("COMPLETED", event.getStatus());
    }

    @Test
    void updateEventStatus_whenWithinRange_shouldSetOngoing() {
        Event event = new Event();
        event.setStatus("UPCOMING");
        event.setStartTime(LocalDateTime.now().minusHours(1));
        event.setEndTime(LocalDateTime.now().plusHours(2));

        EventServiceImpl.updateEventStatus(event, LocalDateTime.now());

        assertEquals("ONGOING", event.getStatus());
    }

    @Test
    void updateEventStatus_whenDatesNull_shouldSetDraft() {
        Event event = new Event();
        event.setStatus("UPCOMING");

        EventServiceImpl.updateEventStatus(event, LocalDateTime.now());

        assertEquals("DRAFT", event.getStatus());
    }
}
