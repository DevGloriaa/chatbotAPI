package com.example.chatbotapi.service;

import com.example.chatbotapi.model.Task;
import java.util.List;

public interface OptimusService {

    List<Task> getTodayTasks(String bearerToken);

    List<Task> getTodayTasksByEmail(String email, String bearerToken);

    String getEmailFromToken(String bearerToken);


    String generateOptimusToken(String email);
}
