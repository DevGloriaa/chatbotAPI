package com.example.chatbotapi.service;

import com.example.chatbotapi.dto.ChatResponse;

public interface ChatService {
    ChatResponse getChatResponse(String message);
}
