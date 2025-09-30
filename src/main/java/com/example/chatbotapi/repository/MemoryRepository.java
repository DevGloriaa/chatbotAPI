package com.example.chatbotapi.repository;

import com.example.chatbotapi.model.Memory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemoryRepository extends JpaRepository<Memory, Long> {
}