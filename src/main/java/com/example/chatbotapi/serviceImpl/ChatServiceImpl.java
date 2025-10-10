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
import java.util.Arrays;
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
    public ChatResponse getChatResponse(String message, String bearerToken) {
        try {
            String lowerMsg = message.toLowerCase();


            if (lowerMsg.contains("today") && lowerMsg.contains("task")) {
                List<Task> tasks = fetchTodayTasks(bearerToken);

                String botReply;
                if (tasks == null || tasks.isEmpty()) {
                    botReply = "✅ You don’t have any tasks scheduled for today. 🎉";
                } else {
                    StringBuilder sb = new StringBuilder("🗓 Here’s your schedule for today:\n\n");
                    for (Task task : tasks) {
                        sb.append("• ").append(task.getTitle());
                        if (task.getDueDate() != null)
                            sb.append(" (Due: ").append(task.getDueDate()).append(")");
                        if (task.getDescription() != null && !task.getDescription().isEmpty())
                            sb.append("\n  ↳ ").append(task.getDescription());
                        sb.append("\n\n");
                    }
                    botReply = sb.toString();
                }

                saveMemory(message, botReply, "tasks", bearerToken);
                return new ChatResponse(botReply);
            }


            ChatResponse aiResponse = callGoogleAI(message);
            saveMemory(message, aiResponse.getText(), "general", bearerToken);
            return aiResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Sorry, I couldn’t process your request right now.");
        }
    }

    private List<Task> fetchTodayTasks(String bearerToken) {
        try {
            String url = optimusBaseUrl + "/tasks/today";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(bearerToken);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Task[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Task[].class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Arrays.asList(response.getBody());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to fetch today's tasks: " + e.getMessage());
        }
        return List.of();
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
            memoryRepository.save(memory);
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
            return new ChatResponse("⚠️ Couldn’t connect. Check your internet connection");
        }
    }
}
