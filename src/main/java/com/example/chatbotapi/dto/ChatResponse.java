package com.example.chatbotapi.dto;

import lombok.Data;

@Data
public class ChatResponse {
    private String message;

    public ChatResponse() {}

    public ChatResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
