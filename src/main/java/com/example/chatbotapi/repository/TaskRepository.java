package com.example.chatbotapi.repository;


import com.example.chatbotapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByEmailAndDueDate(String email, LocalDate dueDate);

}
