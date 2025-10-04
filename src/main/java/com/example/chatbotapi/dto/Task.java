package com.example.chatbotapi.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Task {
    private String id;
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDate dueDate;
    private String time;
    private boolean completed;
}
