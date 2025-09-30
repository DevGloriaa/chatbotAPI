package com.example.chatbotapi.service;

import com.example.chatbotapi.model.User;

public interface UserService {
    User register(String username, String password, String displayName);
    User login(String username, String password);
    String generateToken(User user);
}
