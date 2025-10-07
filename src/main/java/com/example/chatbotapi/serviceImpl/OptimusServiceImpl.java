package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.OptimusService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;

@Service
public class OptimusServiceImpl implements OptimusService {

    private final WebClient webClient;

    public OptimusServiceImpl(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://taskmanagerapi-1-142z.onrender.com/api")
                .build();
    }

    @Override
    public List<Task> getTodayTasks(String bearerToken) {
        try {
            if (bearerToken == null || bearerToken.isEmpty()) {
                System.err.println("⚠️ No bearer token provided!");
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

            if (tasks == null) {
                System.err.println("⚠️ Tasks returned null");
                return Collections.emptyList();
            }

            return tasks;
        } catch (WebClientResponseException e) {
            System.err.println("❌ Error fetching today's tasks: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("❌ Unexpected error fetching today's tasks: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
