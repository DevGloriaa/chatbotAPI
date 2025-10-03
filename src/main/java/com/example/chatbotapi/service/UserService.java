package com.example.chatbotapi.service;

import com.example.chatbotapi.model.User;

public interface UserService {
    User register(String email, String password, String displayName);
    User login(String email, String password);
    String generateToken(User user);
}
