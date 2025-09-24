package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.OptimusService;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class OptimusServiceImpl implements OptimusService {
    private final WebClient webClient;

    public OptimusServiceImpl(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8081").build();
    }

    @Override
    public List<Task> getTodayTasks(String bearerToken) {
        return webClient.get()
                .uri("/tasks/today")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken)
                .retrieve()
                .bodyToFlux(Task.class)
                .collectList()
                .block();
    }
}
