package com.example.chatbotapi.repository;

import com.example.chatbotapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {


    boolean existsByEmail(String email);


    Optional<User> findByEmail(String email);

    String email(String email);
}
