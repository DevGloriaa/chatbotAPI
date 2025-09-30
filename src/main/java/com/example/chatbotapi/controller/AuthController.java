package com.example.chatbotapi.controller;

import com.example.chatbotapi.dto.LoginRequest;
import com.example.chatbotapi.dto.RegisterRequest;
import com.example.chatbotapi.model.User;
import com.example.chatbotapi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest request) {
        User newUser = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getDisplayName()
        );
        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest request) {
        User loggedInUser = userService.login(
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.ok(loggedInUser);
    }
}
