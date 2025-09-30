package com.example.chatbotapi.service;

import com.example.chatbotapi.model.Memory;

import java.util.List;

public interface MemoryService {
    Memory saveMemory(String userMessage, String botResponse);
    List<Memory> getAllMemories();
}