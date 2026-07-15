package com.eventqr.controller;

import com.eventqr.dto.EventRequest;
import com.eventqr.model.Event;
import com.eventqr.service.EventService;
import com.eventqr.service.EventServiceImpl;
import com.eventqr.util.ImageUrlConverter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private Event createTestEvent() {
        Event event = new Event();
        event.setEventId(1L);
        event.setTitle("Test Event");
        event.setOrganizerId(1L);
        event.setLocation("Test Location");
        event.setImageUrl("test.jpg");
        event.setStartTime(LocalDateTime.now().plusDays(1));
        event.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        event.setStatus("UPCOMING");
        return event;
    }

    private EventRequest createValidEventRequest() {
        EventRequest request = new EventRequest();
        request.setTitle("Test Event");
        request.setEventDate(LocalDateTime.now().plusDays(1));
        request.setLocation("Test Location");
        request.setOrganizerId(1L);
        return request;
    }

    @Test
    void createEvent_whenMissingFields_shouldReturnBadRequest() {
        EventRequest request = new EventRequest();
        request.setTitle(null);
        request.setEventDate(null);
        request.setLocation(null);

        ResponseEntity<?> response = eventController.createEvent(request, 1L, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Vui lòng cung cấp đầy đủ thông tin bắt buộc.", response.getBody());
        verifyNoInteractions(eventService);
    }

    @Test
    void createEvent_whenMissingOrganizerId_shouldReturnBadRequest() {
        EventRequest request = createValidEventRequest();
        request.setOrganizerId(null);

        ResponseEntity<?> response = eventController.createEvent(request, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        verifyNoInteractions(eventService);
    }

    @Test
    void createEvent_whenSuccess_shouldReturnCreated() throws Exception {
        EventRequest request = createValidEventRequest();
        Event event = createTestEvent();
        when(eventService.createEvent(any(EventRequest.class))).thenReturn(event);

        try (MockedStatic<EventServiceImpl> eventServiceStatic = mockStatic(EventServiceImpl.class);
             MockedStatic<ImageUrlConverter> imageUrlConverterStatic = mockStatic(ImageUrlConverter.class)) {
            eventServiceStatic.when(() -> EventServiceImpl.updateEventStatus(any(Event.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> null);
            imageUrlConverterStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl(anyString()))
                    .thenReturn(null);

            ResponseEntity<?> response = eventController.createEvent(request, 1L, null);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(event, response.getBody());
            verify(eventService).createEvent(request);
        }
    }

    @Test
    void createEvent_whenException_shouldReturnInternalServerError() throws Exception {
        EventRequest request = createValidEventRequest();
        when(eventService.createEvent(any(EventRequest.class))).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = eventController.createEvent(request, 1L, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void updateEvent_whenMissingOrganizerId_shouldReturnBadRequest() {
        ResponseEntity<?> response = eventController.updateEvent(1L, new EventRequest(), null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Thiếu thông tin organizerId", response.getBody());
        verifyNoInteractions(eventService);
    }

    @Test
    void updateEvent_whenSuccess_shouldReturnOk() throws Exception {
        EventRequest request = createValidEventRequest();
        Event event = createTestEvent();
        when(eventService.updateEvent(anyLong(), any(EventRequest.class), anyLong())).thenReturn(event);

        try (MockedStatic<EventServiceImpl> eventServiceStatic = mockStatic(EventServiceImpl.class);
             MockedStatic<ImageUrlConverter> imageUrlConverterStatic = mockStatic(ImageUrlConverter.class)) {
            eventServiceStatic.when(() -> EventServiceImpl.updateEventStatus(any(Event.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> null);
            imageUrlConverterStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl(anyString()))
                    .thenReturn(null);

            ResponseEntity<?> response = eventController.updateEvent(1L, request, 1L, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(event, response.getBody());
            verify(eventService).updateEvent(1L, request, 1L);
        }
    }

    @Test
    void updateEvent_whenIllegalStateException_shouldReturnForbidden() throws Exception {
        when(eventService.updateEvent(anyLong(), any(EventRequest.class), anyLong()))
                .thenThrow(new IllegalStateException("Bạn không có quyền chỉnh sửa sự kiện này"));

        ResponseEntity<?> response = eventController.updateEvent(1L, new EventRequest(), 1L, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Bạn không có quyền chỉnh sửa sự kiện này", response.getBody());
    }

    @Test
    void updateEvent_whenRuntimeException_shouldReturnNotFound() throws Exception {
        when(eventService.updateEvent(anyLong(), any(EventRequest.class), anyLong()))
                .thenThrow(new RuntimeException("Không tìm thấy sự kiện có ID: 1"));

        ResponseEntity<?> response = eventController.updateEvent(1L, new EventRequest(), 1L, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Không tìm thấy sự kiện có ID: 1", response.getBody());
    }

    @Test
    void updateEvent_whenException_shouldReturnInternalServerError() throws Exception {
        when(eventService.updateEvent(anyLong(), any(EventRequest.class), anyLong()))
                .thenThrow(new Exception("Unexpected error"));

        ResponseEntity<?> response = eventController.updateEvent(1L, new EventRequest(), 1L, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Lỗi khi cập nhật sự kiện: Unexpected error", response.getBody());
    }

    @Test
    void getEventById_whenSuccessWithOrganizerIdMatch_shouldReturnOk() {
        Event event = createTestEvent();
        when(eventService.getEventById(1L)).thenReturn(event);

        try (MockedStatic<EventServiceImpl> eventServiceStatic = mockStatic(EventServiceImpl.class);
             MockedStatic<ImageUrlConverter> imageUrlConverterStatic = mockStatic(ImageUrlConverter.class)) {
            eventServiceStatic.when(() -> EventServiceImpl.updateEventStatus(any(Event.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> null);
            imageUrlConverterStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl(anyString()))
                    .thenReturn(null);

            ResponseEntity<?> response = eventController.getEventById(1L, 1L, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(event, response.getBody());
        }
    }

    @Test
    void getEventById_whenSuccessNoOrganizerId_shouldReturnOk() {
        Event event = createTestEvent();
        when(eventService.getEventById(1L)).thenReturn(event);

        try (MockedStatic<EventServiceImpl> eventServiceStatic = mockStatic(EventServiceImpl.class);
             MockedStatic<ImageUrlConverter> imageUrlConverterStatic = mockStatic(ImageUrlConverter.class)) {
            eventServiceStatic.when(() -> EventServiceImpl.updateEventStatus(any(Event.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> null);
            imageUrlConverterStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl(anyString()))
                    .thenReturn(null);

            ResponseEntity<?> response = eventController.getEventById(1L, null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(event, response.getBody());
        }
    }

    @Test
    void getEventById_whenForbidden_shouldReturnForbidden() {
        Event event = createTestEvent();
        event.setOrganizerId(2L);
        when(eventService.getEventById(1L)).thenReturn(event);

        try (MockedStatic<EventServiceImpl> eventServiceStatic = mockStatic(EventServiceImpl.class);
             MockedStatic<ImageUrlConverter> imageUrlConverterStatic = mockStatic(ImageUrlConverter.class)) {
            eventServiceStatic.when(() -> EventServiceImpl.updateEventStatus(any(Event.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> null);
            imageUrlConverterStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl(anyString()))
                    .thenReturn(null);

            ResponseEntity<?> response = eventController.getEventById(1L, 1L, null);

            assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
            Map<String, Object> body = (Map<String, Object>) response.getBody();
            assertNotNull(body);
            assertFalse((Boolean) body.get("success"));
        }
    }

    @Test
    void getEventById_whenNotFound_shouldReturnNotFound() {
        when(eventService.getEventById(1L)).thenThrow(new RuntimeException("Không tìm thấy sự kiện có ID: 1"));

        ResponseEntity<?> response = eventController.getEventById(1L, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void getEventById_whenException_shouldReturnNotFound() {
        when(eventService.getEventById(1L)).thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> response = eventController.getEventById(1L, null, null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void getAllEvents_whenSuccess_shouldReturnOk() {
        List<Event> events = List.of(createTestEvent());
        when(eventService.findAll()).thenReturn(events);

        try (MockedStatic<EventServiceImpl> eventServiceStatic = mockStatic(EventServiceImpl.class);
             MockedStatic<ImageUrlConverter> imageUrlConverterStatic = mockStatic(ImageUrlConverter.class)) {
            eventServiceStatic.when(() -> EventServiceImpl.updateEventStatus(any(Event.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> null);
            imageUrlConverterStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl(anyString()))
                    .thenReturn(null);

            ResponseEntity<List<Event>> response = eventController.getAllEvents();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(events, response.getBody());
        }
    }

    @Test
    void getAllEvents_whenException_shouldReturnInternalServerError() {
        when(eventService.findAll()).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<List<Event>> response = eventController.getAllEvents();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getMyEvents_whenMissingOrganizerId_shouldReturnBadRequest() {
        ResponseEntity<?> response = eventController.getMyEvents(null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        verifyNoInteractions(eventService);
    }

    @Test
    void getMyEvents_whenSuccess_shouldReturnOk() {
        List<Event> events = List.of(createTestEvent());
        when(eventService.findByOrganizerId(1L)).thenReturn(events);

        try (MockedStatic<EventServiceImpl> eventServiceStatic = mockStatic(EventServiceImpl.class);
             MockedStatic<ImageUrlConverter> imageUrlConverterStatic = mockStatic(ImageUrlConverter.class)) {
            eventServiceStatic.when(() -> EventServiceImpl.updateEventStatus(any(Event.class), any(LocalDateTime.class)))
                    .thenAnswer(invocation -> null);
            imageUrlConverterStatic.when(() -> ImageUrlConverter.convertToAccessibleUrl(anyString()))
                    .thenReturn(null);

            ResponseEntity<?> response = eventController.getMyEvents(1L, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(events, response.getBody());
        }
    }

    @Test
    void getMyEvents_whenException_shouldReturnInternalServerError() {
        when(eventService.findByOrganizerId(1L)).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = eventController.getMyEvents(1L, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
    }
}
