package com.eventqr.controller;

import com.eventqr.dto.CompletedEventResponse;
import com.eventqr.dto.FeedbackReplyRequest;
import com.eventqr.dto.FeedbackRequest;
import com.eventqr.dto.FeedbackResponse;
import com.eventqr.model.Feedback;
import com.eventqr.repository.FeedbackRepository;
import com.eventqr.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest {

    @Mock
    private FeedbackService feedbackService;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackController feedbackController;

    private FeedbackResponse createFeedbackResponse() {
        FeedbackResponse r = new FeedbackResponse();
        r.setFeedbackId(1L);
        r.setEventId(100L);
        r.setEventTitle("Test Event");
        r.setUserId(1L);
        r.setUserName("Test User");
        r.setRating(5);
        r.setComment("Great event!");
        r.setCreatedAt(LocalDateTime.now());
        return r;
    }

    private CompletedEventResponse createCompletedEventResponse() {
        return new CompletedEventResponse(100L, "Test Event", "Location", null,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                "COMPLETED", 1L, LocalDateTime.now().minusDays(3), false);
    }

    private Feedback createFeedback() {
        Feedback f = new Feedback(100L, 1L, 5, "Great event!");
        f.setFeedbackId(1L);
        f.setCreatedAt(LocalDateTime.of(2025, 6, 15, 10, 30));
        return f;
    }

    @Test
    void getCompletedEvents_whenSuccess_shouldReturnOk() {
        List<CompletedEventResponse> events = List.of(createCompletedEventResponse());
        when(feedbackService.getCompletedEventsForUser(1L)).thenReturn(events);

        ResponseEntity<?> response = feedbackController.getCompletedEvents(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(events, response.getBody());
    }

    @Test
    void getCompletedEvents_whenException_shouldReturnBadRequest() {
        when(feedbackService.getCompletedEventsForUser(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = feedbackController.getCompletedEvents(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Error", body.get("message"));
    }

    @Test
    void submitFeedback_whenSuccess_shouldReturnOk() {
        FeedbackResponse fbResponse = createFeedbackResponse();
        FeedbackRequest request = new FeedbackRequest(100L, 1L, 5, "Great event!");
        when(feedbackService.submitFeedback(any(FeedbackRequest.class))).thenReturn(fbResponse);

        ResponseEntity<?> response = feedbackController.submitFeedback(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("Cảm ơn bạn đã đánh giá!", body.get("message"));
        assertEquals(fbResponse, body.get("feedback"));
    }

    @Test
    void submitFeedback_whenIllegalArgumentException_shouldReturnBadRequest() {
        FeedbackRequest request = new FeedbackRequest(100L, 1L, 5, "Great event!");
        when(feedbackService.submitFeedback(any(FeedbackRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid input"));

        ResponseEntity<?> response = feedbackController.submitFeedback(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Invalid input", body.get("message"));
    }

    @Test
    void submitFeedback_whenIllegalStateException_shouldReturnForbidden() {
        FeedbackRequest request = new FeedbackRequest(100L, 1L, 5, "Great event!");
        when(feedbackService.submitFeedback(any(FeedbackRequest.class)))
                .thenThrow(new IllegalStateException("Not allowed"));

        ResponseEntity<?> response = feedbackController.submitFeedback(request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Not allowed", body.get("message"));
    }

    @Test
    void submitFeedback_whenException_shouldReturnInternalServerError() {
        FeedbackRequest request = new FeedbackRequest(100L, 1L, 5, "Great event!");
        when(feedbackService.submitFeedback(any(FeedbackRequest.class)))
                .thenThrow(new RuntimeException("Server error"));

        ResponseEntity<?> response = feedbackController.submitFeedback(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertTrue(((String) body.get("message")).contains("Server error"));
    }

    @Test
    void submitFeedbackForEvent_whenMissingUserId_shouldReturnBadRequest() {
        Map<String, Object> body = Map.of("rating", 5, "comment", "Good");

        ResponseEntity<?> response = feedbackController.submitFeedbackForEvent(100L, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("Thiếu thông tin userId", result.get("message"));
        verifyNoInteractions(feedbackService);
    }

    @Test
    void submitFeedbackForEvent_whenMissingRating_shouldReturnBadRequest() {
        Map<String, Object> body = Map.of("userId", 1, "comment", "Good");

        ResponseEntity<?> response = feedbackController.submitFeedbackForEvent(100L, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("Thiếu thông tin rating", result.get("message"));
        verifyNoInteractions(feedbackService);
    }

    @Test
    void submitFeedbackForEvent_whenSuccess_shouldReturnOk() {
        FeedbackResponse fbResponse = createFeedbackResponse();
        when(feedbackService.submitFeedback(any(FeedbackRequest.class))).thenReturn(fbResponse);
        Map<String, Object> body = Map.of("userId", 1, "rating", 5, "comment", "Great event!");

        ResponseEntity<?> response = feedbackController.submitFeedbackForEvent(100L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("Cảm ơn bạn đã đánh giá!", result.get("message"));
        assertEquals(fbResponse, result.get("feedback"));
    }

    @Test
    void submitFeedbackForEvent_whenIllegalArgumentException_shouldReturnBadRequest() {
        when(feedbackService.submitFeedback(any(FeedbackRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid"));
        Map<String, Object> body = Map.of("userId", 1, "rating", 5, "comment", "Great event!");

        ResponseEntity<?> response = feedbackController.submitFeedbackForEvent(100L, body);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("Invalid", result.get("message"));
    }

    @Test
    void submitFeedbackForEvent_whenIllegalStateException_shouldReturnForbidden() {
        when(feedbackService.submitFeedback(any(FeedbackRequest.class)))
                .thenThrow(new IllegalStateException("Not allowed"));
        Map<String, Object> body = Map.of("userId", 1, "rating", 5, "comment", "Great event!");

        ResponseEntity<?> response = feedbackController.submitFeedbackForEvent(100L, body);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("Not allowed", result.get("message"));
    }

    @Test
    void submitFeedbackForEvent_whenException_shouldReturnInternalServerError() {
        when(feedbackService.submitFeedback(any(FeedbackRequest.class)))
                .thenThrow(new RuntimeException("Server error"));
        Map<String, Object> body = Map.of("userId", 1, "rating", 5, "comment", "Great event!");

        ResponseEntity<?> response = feedbackController.submitFeedbackForEvent(100L, body);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("Server error"));
    }

    @Test
    void getFeedbacksByEvent_whenSuccess_shouldReturnOk() {
        List<FeedbackResponse> feedbacks = List.of(createFeedbackResponse());
        when(feedbackService.getFeedbacksByEventId(100L)).thenReturn(feedbacks);

        ResponseEntity<?> response = feedbackController.getFeedbacksByEvent(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(feedbacks, response.getBody());
    }

    @Test
    void getFeedbacksByEvent_whenException_shouldReturnBadRequest() {
        when(feedbackService.getFeedbacksByEventId(100L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = feedbackController.getFeedbacksByEvent(100L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Error", body.get("message"));
    }

    @Test
    void getFeedbacksByUser_whenSuccess_shouldReturnOk() {
        List<FeedbackResponse> feedbacks = List.of(createFeedbackResponse());
        when(feedbackService.getFeedbacksByUserId(1L)).thenReturn(feedbacks);

        ResponseEntity<?> response = feedbackController.getFeedbacksByUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(feedbacks, response.getBody());
    }

    @Test
    void getFeedbacksByUser_whenException_shouldReturnBadRequest() {
        when(feedbackService.getFeedbacksByUserId(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = feedbackController.getFeedbacksByUser(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Error", body.get("message"));
    }

    @Test
    void getFeedbacksByOrganizer_whenSuccess_shouldReturnOk() {
        List<FeedbackResponse> feedbacks = List.of(createFeedbackResponse());
        when(feedbackService.getFeedbacksByOrganizerId(10L)).thenReturn(feedbacks);

        ResponseEntity<?> response = feedbackController.getFeedbacksByOrganizer(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(feedbacks, response.getBody());
    }

    @Test
    void getFeedbacksByOrganizer_whenException_shouldReturnBadRequest() {
        when(feedbackService.getFeedbacksByOrganizerId(10L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = feedbackController.getFeedbacksByOrganizer(10L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Error", body.get("message"));
    }

    @Test
    void replyFeedback_whenMissingOrganizerId_shouldReturnBadRequest() {
        FeedbackReplyRequest request = new FeedbackReplyRequest(null, "Thanks!");

        ResponseEntity<?> response = feedbackController.replyFeedback(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Thiếu thông tin organizerId", body.get("message"));
        verifyNoInteractions(feedbackService);
    }

    @Test
    void replyFeedback_whenSuccess_shouldReturnOk() {
        FeedbackResponse fbResponse = createFeedbackResponse();
        FeedbackReplyRequest request = new FeedbackReplyRequest(10L, "Thanks!");
        when(feedbackService.replyFeedback(anyLong(), anyLong(), anyString())).thenReturn(fbResponse);

        ResponseEntity<?> response = feedbackController.replyFeedback(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue((Boolean) body.get("success"));
        assertEquals("Đã gửi phản hồi thành công!", body.get("message"));
        assertEquals(fbResponse, body.get("feedback"));
    }

    @Test
    void replyFeedback_whenIllegalArgumentException_shouldReturnBadRequest() {
        FeedbackReplyRequest request = new FeedbackReplyRequest(10L, "");
        when(feedbackService.replyFeedback(anyLong(), anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Invalid reply"));

        ResponseEntity<?> response = feedbackController.replyFeedback(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Invalid reply", body.get("message"));
    }

    @Test
    void replyFeedback_whenIllegalStateException_shouldReturnBadRequest() {
        FeedbackReplyRequest request = new FeedbackReplyRequest(10L, "Thanks!");
        when(feedbackService.replyFeedback(anyLong(), anyLong(), anyString()))
                .thenThrow(new IllegalStateException("Not authorized"));

        ResponseEntity<?> response = feedbackController.replyFeedback(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertEquals("Not authorized", body.get("message"));
    }

    @Test
    void replyFeedback_whenException_shouldReturnInternalServerError() {
        FeedbackReplyRequest request = new FeedbackReplyRequest(10L, "Thanks!");
        when(feedbackService.replyFeedback(anyLong(), anyLong(), anyString()))
                .thenThrow(new RuntimeException("Server error"));

        ResponseEntity<?> response = feedbackController.replyFeedback(1L, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertFalse((Boolean) body.get("success"));
        assertTrue(((String) body.get("message")).contains("Server error"));
    }

    @Test
    void exportFeedbacksPdf_whenSuccessWithFeedbacks_shouldReturnPdf() {
        List<Feedback> feedbacks = List.of(createFeedback());
        when(feedbackRepository.findByEventIdOrderByCreatedAtDesc(100L)).thenReturn(feedbacks);

        ResponseEntity<byte[]> response = feedbackController.exportFeedbacksPdf(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        HttpHeaders headers = response.getHeaders();
        assertEquals(MediaType.APPLICATION_PDF, headers.getContentType());
        assertNotNull(headers.getContentDisposition());
    }

    @Test
    void exportFeedbacksPdf_whenSuccessWithEmptyFeedbacks_shouldReturnPdf() {
        when(feedbackRepository.findByEventIdOrderByCreatedAtDesc(100L)).thenReturn(List.of());

        ResponseEntity<byte[]> response = feedbackController.exportFeedbacksPdf(100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        HttpHeaders headers = response.getHeaders();
        assertEquals(MediaType.APPLICATION_PDF, headers.getContentType());
    }

    @Test
    void exportFeedbacksPdf_whenException_shouldReturnInternalServerError() {
        when(feedbackRepository.findByEventIdOrderByCreatedAtDesc(100L))
                .thenThrow(new RuntimeException("PDF error"));

        ResponseEntity<byte[]> response = feedbackController.exportFeedbacksPdf(100L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }
}
