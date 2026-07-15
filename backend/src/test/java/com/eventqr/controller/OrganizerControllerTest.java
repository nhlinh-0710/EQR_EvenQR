package com.eventqr.controller;

import com.eventqr.dto.OrganizerFeedbackResponse;
import com.eventqr.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizerControllerTest {

    @Mock
    private FeedbackService feedbackService;

    @InjectMocks
    private OrganizerController controller;

    @Test
    void getOrganizerFeedbacks_shouldReturnFeedbacks() {
        List<OrganizerFeedbackResponse> feedbacks = List.of(new OrganizerFeedbackResponse());
        when(feedbackService.getOrganizerFeedbacks(1L)).thenReturn(feedbacks);

        var response = controller.getOrganizerFeedbacks(1L);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertEquals(feedbacks, response.getBody());
    }

    @Test
    void getOrganizerFeedbacks_whenException_shouldReturnBadRequest() {
        when(feedbackService.getOrganizerFeedbacks(1L)).thenThrow(new RuntimeException("error"));

        var response = controller.getOrganizerFeedbacks(1L);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }
}
