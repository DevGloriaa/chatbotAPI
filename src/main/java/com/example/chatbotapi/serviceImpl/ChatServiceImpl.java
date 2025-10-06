package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.ChatService;
import com.example.chatbotapi.service.OptimusService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final OptimusService optimusService;
    private final RestTemplate restTemplate;

    private static final String MODEL = "gemini-2.5-flash";
    private final String ENDPOINT;
    private final String googleApiKey;

    public ChatServiceImpl(
            OptimusService optimusService,
            @Value("${GOOGLE_API_KEY}") String googleApiKey
    ) {
        this.optimusService = optimusService;
        this.restTemplate = new RestTemplate();
        this.googleApiKey = googleApiKey;


        this.ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/"
                + MODEL + ":generateContent?key=" + googleApiKey;

        System.out.println("✅ Google API key loaded: " + (googleApiKey != null ? "FOUND ✅" : "❌ MISSING ❌"));
        System.out.println("🌐 Gemini endpoint: " + ENDPOINT);
    }

    @Override
    public ChatResponse getChatResponse(String message, String authHeader) {
        try {
            String lowerMsg = message.toLowerCase();

            if (lowerMsg.contains("today's tasks")
                    || lowerMsg.contains("scheduled for today")
                    || lowerMsg.contains("tasks for today")) {

                List<Task> tasks = optimusService.getTodayTasks(authHeader);

                if (tasks == null || tasks.isEmpty()) {
                    return new ChatResponse("✅ You don’t have any tasks for today.");
                }

                StringBuilder reply = new StringBuilder("Here’s your schedule for today:\n");
                for (Task task : tasks) {
                    reply.append("- ").append(task.getTitle()).append("\n");
                }
                return new ChatResponse(reply.toString());
            }

            return callGoogleAI(message);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Sorry, I couldn’t process your request right now.");
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
            return new ChatResponse("⚠️ Couldn’t connect to Gemini API. Please check your API key or internet connection.");
        }
    }
}
