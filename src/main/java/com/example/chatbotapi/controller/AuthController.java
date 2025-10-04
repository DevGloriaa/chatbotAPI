package com.example.chatbotapi.controller;

import com.example.chatbotapi.dto.AuthResponse;
import com.example.chatbotapi.dto.LoginRequest;
import com.example.chatbotapi.dto.RegisterRequest;
import com.example.chatbotapi.model.User;
import com.example.chatbotapi.repository.UserRepository;
import com.example.chatbotapi.service.UserService;
import com.example.chatbotapi.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public AuthController(UserService userService, UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

        User newUser = userService.register(
                request.getEmail(),
                encodedPassword,
                request.getDisplayName()
        );

        String token = userService.generateToken(newUser);
        return ResponseEntity.ok(new AuthResponse(token, newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "User not found"));
        }

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }

        String token = JwtUtil.generateToken(user.getEmail());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", user.getEmail(),
                "displayName", user.getDisplayName()
        ));
    }
}
