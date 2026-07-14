package com.eventqr.controller;

import com.eventqr.dto.ChatbotRequest;
import com.eventqr.dto.ChatbotResponse;
import com.eventqr.service.ChatbotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatbotRequest request) {
        try {
            if (request == null || request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Vui lòng nhập câu hỏi"));
            }

            ChatbotResponse response = chatbotService.processMessage(request);
            
            if (response.isSuccess()) {
                // Return consistent format
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", response.getMessage(),
                    "conversationId", response.getConversationId() != null ? response.getConversationId() : ""
                ));
            } else {
                return ResponseEntity.status(500)
                    .body(Map.of("success", false, "message", response.getMessage(), "error", response.getError() != null ? response.getError() : ""));
            }
        } catch (Exception e) {
            logger.error("❌ Lỗi xử lý chatbot request: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "message", "Lỗi xử lý yêu cầu: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "chatbot"));
    }
}

