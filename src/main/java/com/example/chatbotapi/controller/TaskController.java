package com.example.chatbotapi.controller;

import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.OptimusService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final OptimusService optimusService;

    public TaskController(OptimusService optimusService) {
        this.optimusService = optimusService;
    }

    @GetMapping("/today")
    public List<Task> getTodayTasks(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken) {
        if (!bearerToken.startsWith("Bearer ")) {
            bearerToken = "Bearer " + bearerToken;
        }
        return optimusService.getTodayTasks(bearerToken);
    }
}
