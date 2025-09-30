package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.model.Memory;
import com.example.chatbotapi.repository.MemoryRepository;
import com.example.chatbotapi.service.MemoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemoryServiceImpl implements MemoryService {

    private final MemoryRepository memoryRepository;

    public MemoryServiceImpl(MemoryRepository memoryRepository) {
        this.memoryRepository = memoryRepository;
    }

    @Override
    public Memory saveMemory(String userMessage, String botResponse, String topic) {
        Memory memory = new Memory();
        memory.setUserMessage(userMessage);
        memory.setBotResponse(botResponse);
        memory.setTopic(topic);
        return memoryRepository.save(memory);
    }

    @Override
    public List<Memory> getAllMemories() {
        return memoryRepository.findAll();
    }

    @Override
    public List<Memory> getMemoriesByTopic(String topic) {
        return memoryRepository.findByTopicOrderByCreatedAtDesc(topic);
    }

    @Override
    public List<Memory> searchMemories(String keyword) {
        return memoryRepository.findByUserMessageContainingIgnoreCase(keyword);
    }
}