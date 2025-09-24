package com.example.chatbotapi.service;


import com.example.chatbotapi.dto.Task;

import java.util.List;

public interface OptimusService {
    List<Task> getTodayTasks();
}