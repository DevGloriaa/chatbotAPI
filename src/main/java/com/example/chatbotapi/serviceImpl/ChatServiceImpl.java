package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.dto.HuggingFaceResponse;
import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.ChatService;
import com.example.chatbotapi.service.OptimusService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final String apiKey;
    private final WebClient webClient;
    private final OptimusService optimusService;

    public ChatServiceImpl(OptimusService optimusService) {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("HF_API_KEY");

        this.webClient = WebClient.builder()
                .baseUrl("https://api-inference.huggingface.co/models/gpt2")
                .defaultHeader("Content-Type", "application/json")
                .build();

        this.optimusService = optimusService;
    }

    @Override
    public ChatResponse getChatResponse(String message) {
        try {
            if (message.toLowerCase().contains("today's tasks")
                    || message.toLowerCase().contains("scheduled for today")
                    || message.toLowerCase().contains("tasks for today")) {

                List<Task> tasks = optimusService.getTodayTasks();

                if (tasks == null || tasks.isEmpty()) {
                    return new ChatResponse("You have no tasks scheduled for today 🎉");
                }

                StringBuilder reply = new StringBuilder("Here’s your schedule for today:\n");
                tasks.forEach(task -> reply.append("- ")
                        .append(task.getDueDate() != null ? task.getDueDate() + " " : "")
                        .append(task.getTitle())
                        .append("\n"));

                return new ChatResponse(reply.toString());
            }

            String requestBody = """
                {
                    "inputs": "%s"
                }
            """.formatted(message);

            HuggingFaceResponse[] hfResponse = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(HuggingFaceResponse[].class)
                    .block();

            if (hfResponse != null && hfResponse.length > 0) {
                return new ChatResponse(hfResponse[0].getGeneratedText());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ChatResponse("Sorry, I couldn’t generate a response.");
    }
}
