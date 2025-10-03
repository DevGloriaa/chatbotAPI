package com.example.chatbotapi.controller;

import com.example.chatbotapi.dto.AuthResponse;
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
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        User newUser = userService.register(
                request.getEmail(),
                request.getPassword(),
                request.getDisplayName()
        );

        String token = userService.generateToken(newUser);
        return ResponseEntity.ok(new AuthResponse(token, newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        User loggedInUser = userService.login(
                request.getEmail(),
                request.getPassword()
        );

        String token = userService.generateToken(loggedInUser);
        return ResponseEntity.ok(new AuthResponse(token, loggedInUser));
    }
}