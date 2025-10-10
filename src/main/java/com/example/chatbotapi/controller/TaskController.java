package com.example.chatbotapi.controller;

import com.example.chatbotapi.model.Task;
import com.example.chatbotapi.utils.JwtUtil;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String OPTIMUS_URL = "https://taskmanagerapi-1-142z.onrender.com/api/tasks/today";

    @GetMapping("/today")
    public ResponseEntity<String> getTodayTasks(@RequestHeader("Authorization") String authHeader) {
        System.out.println("🚀 Received request to /api/tasks/today");

        try {

            String Token = authHeader.replace("Bearer ", "").trim();
            System.out.println("🔐 Incoming Kos token: " + Token);


            String email = JwtUtil.getEmailFromToken(Token);
            System.out.println("📧 Extracted email from token: " + email);

            if (email == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("❌ Invalid Kos token: email not found.");
            }

            String optimusToken = JwtUtil.generateToken(email);
            System.out.println("🎟️ Generated Optimus token: " + optimusToken);


            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + optimusToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            System.out.println("🌍 Sending request to Optimus...");

            ResponseEntity<Task[]> response = restTemplate.exchange(
                    OPTIMUS_URL,
                    HttpMethod.GET,
                    entity,
                    Task[].class
            );

            Task[] tasks = response.getBody();
            if (tasks == null || tasks.length == 0) {
                return ResponseEntity.ok("✅ You don’t have any tasks today.");
            }

            StringBuilder sb = new StringBuilder("📋 Here are your tasks:\n");
            for (int i = 0; i < tasks.length; i++) {
                sb.append(i + 1).append(". ").append(tasks[i].getTitle()).append("\n");
            }

            return ResponseEntity.ok(sb.toString());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            System.out.println("❌ Optimus API error: " + e.getStatusCode());
            System.out.println("🧾 Response body: " + e.getResponseBodyAsString());

            return ResponseEntity.status(e.getStatusCode())
                    .body("⚠️ Optimus API returned an error: " + e.getStatusCode() +
                            "\nResponse: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("⚠️ Failed to fetch tasks from Optimus: " + e.getMessage());
        }
    }
}
