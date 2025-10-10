package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.model.Task;
import com.example.chatbotapi.service.OptimusService;
import com.example.chatbotapi.utils.JwtUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OptimusServiceImpl implements OptimusService {

    private final WebClient webClient;

    public OptimusServiceImpl(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://taskmanagerapi-2-s90z.onrender.com")
                .build();
    }

    @Override
    public List<Task> getTodayTasks(String bearerToken) {
        try {
            if (bearerToken == null || bearerToken.isEmpty()) return Collections.emptyList();

            String fullToken = bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken;

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
    public List<Task> getTodayTasksByEmail(String email, String bearerToken) {
        try {
            LocalDate today = LocalDate.now();
            String fullToken = bearerToken.startsWith("Bearer ") ? bearerToken : "Bearer " + bearerToken;

            List<Task> allTasks = webClient.get()
                    .uri("/tasks/tasks/by-email/{email}", email)
                    .header(HttpHeaders.AUTHORIZATION, fullToken)
                    .retrieve()
                    .bodyToFlux(Task.class)
                    .collectList()
                    .block();

            if (allTasks == null) return Collections.emptyList();

            return allTasks.stream()
                    .filter(task -> today.equals(task.getDueDate()))
                    .collect(Collectors.toList());

        } catch (WebClientResponseException e) {
            System.err.println("❌ Error fetching tasks by email: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error fetching tasks by email: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public String getEmailFromToken(String bearerToken) {
        return JwtUtil.getEmailFromToken(bearerToken);
    }


    @Override
    public String generateOptimusToken(String email) {
        if (email == null || email.isEmpty()) return null;


        String optimusToken = JwtUtil.generateToken(email);
        System.out.println("🎟️ Generated Optimus token for " + email + ": " + optimusToken);
        return optimusToken;
    }
}
