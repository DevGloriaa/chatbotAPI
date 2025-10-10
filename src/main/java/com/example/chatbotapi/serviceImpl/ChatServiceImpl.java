package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.model.Memory;
import com.example.chatbotapi.model.Task;
import com.example.chatbotapi.repository.MemoryRepository;
import com.example.chatbotapi.service.ChatService;
import com.example.chatbotapi.service.OptimusService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final OptimusService optimusService;
    private final MemoryRepository memoryRepository;
    private final RestTemplate restTemplate;
    private final String optimusBaseUrl;
    private final String googleApiKey;
    private static final String MODEL = "gemini-2.5-flash";
    private final String ENDPOINT;

    public ChatServiceImpl(
            OptimusService optimusService,
            MemoryRepository memoryRepository,
            @Value("${OPTIMUS_BASE_URL}") String optimusBaseUrl,
            @Value("${GOOGLE_API_KEY}") String googleApiKey
    ) {
        this.optimusService = optimusService;
        this.memoryRepository = memoryRepository;
        this.restTemplate = new RestTemplate();
        this.optimusBaseUrl = optimusBaseUrl;
        this.googleApiKey = googleApiKey;

        this.ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"
                + MODEL + ":generateContent?key=" + googleApiKey;

        System.out.println("✅ Google API key loaded: " + (googleApiKey != null ? "FOUND ✅" : "❌ MISSING ❌"));
        System.out.println("🌐 Gemini endpoint: " + ENDPOINT);
        System.out.println("🔗 Optimus API Base URL: " + optimusBaseUrl);
    }

    @Override
    public ChatResponse getChatResponse(String message, String kosBearerToken) {
        try {
            String lowerMsg = message.toLowerCase();
            String botReply;

            if (lowerMsg.contains("today") && lowerMsg.contains("task")) {
                botReply = handleTodayTasks(message, kosBearerToken);
            } else {
                botReply = callGoogleAI(message).getText();
            }


            try {
                String email = optimusService.getEmailFromToken(kosBearerToken);
                saveMemory(message, botReply, "general", kosBearerToken);
            } catch (Exception e) {
                System.err.println("⚠️ Skipped memory save: Invalid token");
            }

            return new ChatResponse(botReply);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Sorry, I couldn’t process your request right now.");
        }
    }

    private String handleTodayTasks(String message, String kosBearerToken) {
        try {
            String email = optimusService.getEmailFromToken(kosBearerToken);
            String optimusToken = optimusService.generateOptimusToken(email);
            List<Task> tasks = optimusService.getTodayTasks(optimusToken);

            if (tasks == null || tasks.isEmpty()) {
                return "✅ You don’t have any tasks scheduled for today. 🎉";
            }

            StringBuilder sb = new StringBuilder("🗓 Here’s your schedule for today:\n\n");
            for (Task task : tasks) {
                sb.append("• ").append(task.getTitle());
                if (task.getDueDate() != null)
                    sb.append(" (Due: ").append(task.getDueDate()).append(")");
                if (task.getDescription() != null && !task.getDescription().isEmpty())
                    sb.append("\n  ↳ ").append(task.getDescription());
                sb.append("\n\n");
            }
            return sb.toString();

        } catch (Exception e) {
            System.err.println("⚠️ Could not fetch tasks: " + e.getMessage());
            return "⚠️ Sorry, I couldn’t fetch your tasks right now.";
        }
    }

    @Override
    public void saveMemory(String userMessage, String botResponse, String topic, String bearerToken) {
        try {
            if (botResponse.length() > 2000)
                botResponse = botResponse.substring(0, 2000);

            String email = optimusService.getEmailFromToken(bearerToken);

            Memory memory = new Memory();
            memory.setUserMessage(userMessage);
            memory.setBotResponse(botResponse);
            memory.setTopic(topic != null ? topic : "general");
            memory.setEmail(email);
            memory.setCreatedAt(LocalDateTime.now());

            if (userMessage.toLowerCase().contains("remember") || userMessage.toLowerCase().contains("note")) {
                memory.setMemoryType("long-term");
                memory.setImportance(1.0);
            } else {
                memory.setMemoryType("short-term");
                memory.setImportance(0.5);
            }

            memoryRepository.save(memory);
            System.out.println("💾 Saved " + memory.getMemoryType() + " memory for " + email);

        } catch (Exception e) {
            System.err.println("⚠️ Failed to save memory: " + e.getMessage());
        }
    }

    private ChatResponse callGoogleAI(String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = """
                    {
                      "contents": [
                        {
                          "parts": [
                            {"text": "%s"}
                          ]
                        }
                      ]
                    }
                    """.formatted(message.replace("\"", "\\\""));

            HttpEntity<String> request = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> bodyMap = response.getBody();
                if (bodyMap.containsKey("candidates")) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) bodyMap.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (!parts.isEmpty()) {
                            return new ChatResponse((String) parts.get(0).get("text"));
                        }
                    }
                }
            }

            return new ChatResponse("⚠️ Gemini didn’t return any text output.");

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Couldn’t connect. Check your internet connection.");
        }
    }
}
