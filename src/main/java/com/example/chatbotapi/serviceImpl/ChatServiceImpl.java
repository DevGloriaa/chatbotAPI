package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.model.Task;
import com.example.chatbotapi.model.Memory;
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
    private final RestTemplate restTemplate;
    private final MemoryRepository memoryRepository;

    private static final String MODEL = "gemini-2.5-flash";
    private final String ENDPOINT;
    private final String googleApiKey;

    public ChatServiceImpl(
            OptimusService optimusService,
            MemoryRepository memoryRepository,
            @Value("${GOOGLE_API_KEY}") String googleApiKey
    ) {
        this.optimusService = optimusService;
        this.restTemplate = new RestTemplate();
        this.memoryRepository = memoryRepository;
        this.googleApiKey = googleApiKey;

        this.ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"
                + MODEL + ":generateContent?key=" + googleApiKey;

        System.out.println("✅ Google API key loaded: " + (googleApiKey != null ? "FOUND ✅" : "❌ MISSING ❌"));
        System.out.println("🌐 Gemini endpoint: " + ENDPOINT);
    }

    @Override
    public ChatResponse getChatResponse(String message, String bearerToken) {
        try {
            String lowerMsg = message.toLowerCase();

            // Check if user is asking about tasks
            if (lowerMsg.contains("today's tasks")
                    || lowerMsg.contains("scheduled for today")
                    || lowerMsg.contains("tasks for today")) {

                List<Task> tasks = optimusService.getTodayTasks(bearerToken);

                String botReply;
                if (tasks == null || tasks.isEmpty()) {
                    botReply = "✅ You don’t have any tasks for today.";
                } else {
                    StringBuilder sb = new StringBuilder("Here’s your schedule for today:\n");
                    for (Task task : tasks) {
                        sb.append("- ").append(task.getTitle()).append("\n");
                    }
                    botReply = sb.toString();
                }

                // Save memory
                saveMemory(message, botReply, "tasks", bearerToken);

                return new ChatResponse(botReply);
            }

            // Otherwise call Gemini AI
            ChatResponse aiResponse = callGoogleAI(message);

            // Save memory
            saveMemory(message, aiResponse.getText(), "general", bearerToken);

            return aiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Sorry, I couldn’t process your request right now.");
        }
    }

    @Override
    public void saveMemory(String userMessage, String botResponse, String topic, String bearerToken) {
        try {
            if (botResponse.length() > 2000) {
                botResponse = botResponse.substring(0, 2000); // truncate to prevent DB errors
            }

            String email = optimusService.getEmailFromToken(bearerToken);

            Memory memory = new Memory();
            memory.setUserMessage(userMessage);
            memory.setBotResponse(botResponse);
            memory.setTopic(topic != null ? topic : "general");
            memory.setEmail(email);
            memory.setCreatedAt(LocalDateTime.now());

            memoryRepository.save(memory);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to save memory: " + e.getMessage());
        }
    }

    private ChatResponse callGoogleAI(String message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String body = "{\n" +
                    "  \"contents\": [\n" +
                    "    {\n" +
                    "      \"parts\": [\n" +
                    "        {\"text\": \"" + message.replace("\"", "\\\"") + "\"}\n" +
                    "      ]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            System.out.println("🌐 Sending request to Gemini API...");

            ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> bodyMap = response.getBody();
                System.out.println("🌍 Gemini raw response: " + bodyMap);

                if (bodyMap.containsKey("candidates")) {
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) bodyMap.get("candidates");
                    if (!candidates.isEmpty()) {
                        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (!parts.isEmpty()) {
                            String output = (String) parts.get(0).get("text");
                            return new ChatResponse(output);
                        }
                    }
                }
            }

            return new ChatResponse("⚠️ Gemini didn’t return any text output.");

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Couldn’t connect. Please check your internet connection.");
        }
    }
}
