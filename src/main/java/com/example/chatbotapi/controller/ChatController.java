package com.example.chatbotapi.controller;

import com.example.chatbotapi.dto.ChatRequest;
import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.service.ChatService;
import com.example.chatbotapi.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request,
                                  @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = extractToken(authHeader);
            String email = JwtUtil.getEmailFromToken(token);

            ChatResponse response = chatService.getChatResponse(request.getMessage(), email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized or invalid token: " + e.getMessage()));
        }
    }

    @PostMapping("/map")
    public ResponseEntity<?> chatMap(@RequestBody Map<String, String> payload,
                                     @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = extractToken(authHeader);
            String email = JwtUtil.getEmailFromToken(token);

            String userMessage = payload.get("message");
            ChatResponse response = chatService.getChatResponse(userMessage, email);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized or invalid token: " + e.getMessage()));
        }
    }

    @PostMapping("/memory/save")
    public ResponseEntity<?> saveMemory(@RequestBody Map<String, String> payload,
                                        @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String token = extractToken(authHeader);
            String email = JwtUtil.getEmailFromToken(token);

            String userMessage = payload.get("userMessage");
            String botResponse = payload.get("botResponse");
            String topic = payload.get("topic");

            chatService.saveMemory(userMessage, botResponse, topic, email);

            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Unauthorized or invalid token: " + e.getMessage()));
        }
    }

    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        throw new IllegalArgumentException("Missing or invalid Authorization header");
    }
}
