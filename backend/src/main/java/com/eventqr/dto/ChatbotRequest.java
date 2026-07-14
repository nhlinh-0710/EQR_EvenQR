package com.eventqr.dto;

import lombok.Data;

@Data
public class ChatbotRequest {
    private String message;
    private String conversationId; // Optional: để duy trì context
    private Long userId; // Optional: để personalize responses
}

