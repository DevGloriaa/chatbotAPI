//package com.example.chatbotapi.serviceImpl;
//
//import com.example.chatbotapi.model.Memory;
//import com.example.chatbotapi.repository.MemoryRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.http.*;
//
//import java.util.*;
//
//@Service
//public class HuggingFaceService {
//
//    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/blenderbot-400M-distill";
//
//    @Value("${HF_API_KEY}")
//    private String hfToken;
//
//    private final MemoryRepository memoryRepository;
//
//    public HuggingFaceService(MemoryRepository memoryRepository) {
//        this.memoryRepository = memoryRepository;
//    }
//
//    public String chatWithHuggingFace(String message) {
//        try {
//            RestTemplate restTemplate = new RestTemplate();
//
//
//            List<Memory> history = memoryRepository.findTop5ByOrderByCreatedAtDesc();
//
//
//            StringBuilder context = new StringBuilder();
//            for (int i = history.size() - 1; i >= 0; i--) {
//                Memory mem = history.get(i);
//                context.append("User: ").append(mem.getUserMessage()).append("\n");
//                context.append("Kos: ").append(mem.getBotResponse()).append("\n");
//            }
//            context.append("User: ").append(message);
//
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer " + hfToken);
//
//            Map<String, String> body = new HashMap<>();
//            body.put("inputs", context.toString());
//
//            HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
//
//
//            ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, entity, Map.class);
//
//            String botReply;
//            if (response.getBody() != null && response.getBody().get("generated_text") != null) {
//                botReply = response.getBody().get("generated_text").toString();
//            } else {
//                botReply = "Sorry, I couldn’t generate a reply 😢";
//            }
//
//
//            Memory memory = new Memory();
//            memory.setUserMessage(message);
//            memory.setBotResponse(botReply);
//            memory.setTopic("general");
//            memoryRepository.save(memory);
//
//            return botReply;
//
//        } catch (Exception e) {
//            return "⚠️ Error talking to Hugging Face API: " + e.getMessage();
//        }
//    }
//}
