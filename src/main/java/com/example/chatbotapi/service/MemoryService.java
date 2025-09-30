package com.example.chatbotapi.service;

import com.example.chatbotapi.model.Memory;

import java.util.List;

public interface MemoryService {

    Memory saveMemory(String userMessage, String botResponse, String topic);

    List<Memory> getAllMemories();

    List<Memory> getMemoriesByTopic(String topic);

    List<Memory> searchMemories(String keyword);
}