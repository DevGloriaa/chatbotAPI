package com.example.chatbotapi.repository;

import com.example.chatbotapi.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemoryRepository extends JpaRepository<Memory, Long> {

    List<Memory> findByTopicOrderByCreatedAtDesc(String topic); // fetch memories by topic
    List<Memory> findByUserMessageContainingIgnoreCase(String keyword);
    List<Memory>findTop5ByOrderByCreatedAtDesc();
    List<Memory> findByEmailOrderByCreatedAtAsc(String email);
}