package com.eventqr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotResponse {
    private boolean success;
    private String message;
    private String conversationId;
    private String error; // Optional error message
}

