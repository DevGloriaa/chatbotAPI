package com.example.chatbotapi.controller;

import com.example.chatbotapi.model.Task;
import com.example.chatbotapi.service.OptimusService;
import com.example.chatbotapi.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final OptimusService optimusService;

    public TaskController(OptimusService optimusService) {
        this.optimusService = optimusService;
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayTasks(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            String email = JwtUtil.getEmailFromToken(token);

            System.out.println("📅 Fetching tasks for " + email + " on " + java.time.LocalDate.now());


            List<Task> tasks = optimusService.getTodayTasksByEmail(email, authHeader);

            return ResponseEntity.ok(tasks);

        } catch (Exception e) {
            System.err.println("❌ Error fetching tasks: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching today's tasks: " + e.getMessage());
        }
    }
}
