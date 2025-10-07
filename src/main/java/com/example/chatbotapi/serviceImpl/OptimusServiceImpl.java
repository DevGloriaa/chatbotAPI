package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.model.Task;
import com.example.chatbotapi.repository.TaskRepository;
import com.example.chatbotapi.service.OptimusService;
import com.example.chatbotapi.utils.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class OptimusServiceImpl implements OptimusService {

    private final WebClient webClient;
    private final TaskRepository taskRepository;

    public OptimusServiceImpl(WebClient.Builder builder, TaskRepository taskRepository) {
        this.webClient = builder
                .baseUrl("https://taskmanagerapi-1-142z.onrender.com/api")
                .build();
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Task> getTodayTasks(String bearerToken) {
        try {
            if (bearerToken == null || bearerToken.isEmpty()) {
                System.err.println("⚠️ Bearer token missing!");
                return Collections.emptyList();
            }

            String fullToken = bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken;
            System.out.println("Fetching tasks with token: " + fullToken);

            List<Task> tasks = webClient.get()
                    .uri("/tasks/today")
                    .header(HttpHeaders.AUTHORIZATION, fullToken)
                    .retrieve()
                    .bodyToFlux(Task.class)
                    .collectList()
                    .block();

            return tasks != null ? tasks : Collections.emptyList();

        } catch (WebClientResponseException e) {
            System.err.println("❌ Error fetching today's tasks: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error fetching today's tasks: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<Task> getTodayTasksByEmail(String email) {
        LocalDate today = LocalDate.now();
        return taskRepository.findByEmailAndDate(email, today);
    }

    @Override
    public String getEmailFromToken(String bearerToken) {
        return JwtUtil.getEmailFromToken(bearerToken);
    }
}
