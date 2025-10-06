package com.example.chatbotapi.dto;

public class ChatResponse {
    private String text;

    public ChatResponse() {}

    public ChatResponse(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
