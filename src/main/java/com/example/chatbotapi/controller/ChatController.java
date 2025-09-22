package com.example.chatbotapi.controller;

import com.example.chatbotapi.dto.ChatRequest;
import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        return chatService.getChatResponse(request.getMessage());
    }
}
