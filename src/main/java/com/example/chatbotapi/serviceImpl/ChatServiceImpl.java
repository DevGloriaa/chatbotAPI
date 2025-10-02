package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.ChatService;
import com.example.chatbotapi.service.OptimusService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl implements ChatService {

    private final OptimusService optimusService;
    private final RestTemplate restTemplate;

    // Your Google AI API key
    private static final String GOOGLE_API_KEY = "YOUR_API_KEY";
    private static final String MODEL = "gemini-2.5-flash";
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta2/models/" + MODEL + ":generate";

    public ChatServiceImpl(OptimusService optimusService) {
        this.optimusService = optimusService;
        this.restTemplate = new RestTemplate();
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
                    return new ChatResponse("You have no tasks scheduled for today 🎉");
                }

                StringBuilder reply = new StringBuilder("Here’s your schedule for today:\n");
                for (Task task : tasks) {
                    reply.append("- ")
                            .append(task.getTime() != null ? task.getTime() + " " : "")
                            .append(task.getTitle());

                    if (task.getDate() != null) {
                        reply.append(" (").append(task.getDate()).append(")");
                    }

                    reply.append("\n");
                }
                return new ChatResponse(reply.toString());
            }


            return callGoogleAI(message);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Sorry, I couldn’t fetch a response right now.");
        }
    }

    private ChatResponse callGoogleAI(String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(GOOGLE_API_KEY);

        String body = "{\n" +
                "  \"prompt\": {\"text\": \"" + message.replace("\"", "\\\"") + "\"},\n" +
                "  \"temperature\": 0.8,\n" +
                "  \"maxOutputTokens\": 150\n" +
                "}";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(ENDPOINT, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> bodyMap = response.getBody();
            if (bodyMap.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) bodyMap.get("candidates");
                if (!candidates.isEmpty()) {
                    String output = (String) candidates.get(0).get("output");
                    return new ChatResponse(output);
                }
            }
        }

        return new ChatResponse("⚠️ Sorry, I couldn’t fetch a response from AI.");
    }
}
