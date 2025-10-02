package com.example.chatbotapi.controller;

import com.example.chatbotapi.dto.ChatRequest;
import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }


    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request,
                             @RequestHeader("Authorization") String authHeader) {
        String userMessage = request.getMessage();
        return chatService.getChatResponse(userMessage, authHeader);
    }


    @PostMapping("/map")
    public ChatResponse chatMap(@RequestBody Map<String, String> payload,
                                @RequestHeader("Authorization") String authHeader) {
        String userMessage = payload.get("message");
        return chatService.getChatResponse(userMessage, authHeader);
    }
}
