package com.eventqr.controller;

import com.eventqr.dto.ChatbotRequest;
import com.eventqr.dto.ChatbotResponse;
import com.eventqr.service.ChatbotService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotControllerTest {

    @Mock
    private ChatbotService chatbotService;

    @InjectMocks
    private ChatbotController controller;

    @Test
    void chat_withNullRequest_shouldReturnBadRequest() {
        var response = controller.chat(null);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        verifyNoInteractions(chatbotService);
    }

    @Test
    void chat_withEmptyMessage_shouldReturnBadRequest() {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("   ");

        var response = controller.chat(request);

        assertTrue(response.getStatusCode().is4xxClientError());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        verifyNoInteractions(chatbotService);
    }

    @Test
    void chat_withSuccessResponse_shouldReturnOk() {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("hello");
        when(chatbotService.processMessage(request)).thenReturn(new ChatbotResponse(true, "Hi there!", "conv1", null));

        var response = controller.chat(request);

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertTrue((Boolean) body.get("success"));
        assertEquals("Hi there!", body.get("message"));
        assertEquals("conv1", body.get("conversationId"));
    }

    @Test
    void chat_withErrorResponse_shouldReturn500() {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("hello");
        when(chatbotService.processMessage(request)).thenReturn(new ChatbotResponse(false, "failed", null, "some error"));

        var response = controller.chat(request);

        assertEquals(500, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
        assertEquals("failed", body.get("message"));
    }

    @Test
    void chat_whenException_shouldReturn500() {
        ChatbotRequest request = new ChatbotRequest();
        request.setMessage("hello");
        when(chatbotService.processMessage(request)).thenThrow(new RuntimeException("connection failed"));

        var response = controller.chat(request);

        assertEquals(500, response.getStatusCode().value());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void health_shouldReturnOk() {
        var response = controller.health();

        assertTrue(response.getStatusCode().is2xxSuccessful());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("ok", body.get("status"));
        assertEquals("chatbot", body.get("service"));
    }
}
