package com.example.chatbotapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Task {
    private Long id;
    private String title;
    private LocalDate date;
    private String time;
    private boolean completed;
}