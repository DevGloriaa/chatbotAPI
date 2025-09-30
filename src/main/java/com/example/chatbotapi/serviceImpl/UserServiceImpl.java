package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.model.User;
import com.example.chatbotapi.repository.UserRepository;
import com.example.chatbotapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public User register(String username, String password, String displayName) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken!");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .displayName(displayName)
                .build();

        return userRepository.save(user);
    }

    @Override
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    @Override
    public String generateToken(User user) {
        String tokenData = user.getUsername() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(tokenData.getBytes());
    }
}
