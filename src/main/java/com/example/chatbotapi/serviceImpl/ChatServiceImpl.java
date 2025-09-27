package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.ChatService;
import com.example.chatbotapi.service.OptimusService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final OptimusService optimusService;

    public ChatServiceImpl(OptimusService optimusService) {
        this.optimusService = optimusService;
    }

    @Override
    public ChatResponse getChatResponse(String message, String authHeader) {
        try {
            String lowerMsg = message.toLowerCase();
            if (lowerMsg.contains("today's tasks")
                    || lowerMsg.contains("scheduled for today")
                    || lowerMsg.contains("tasks for today")) {

                List<Task> tasks = optimusService.getTodayTasks(authHeader);

                if (tasks == null || tasks.isEmpty()) {
                    return new ChatResponse("You have no tasks scheduled for today 🎉");
                }

                StringBuilder reply = new StringBuilder("Here’s your schedule for today:\n");
                for (Task task : tasks) {
                    reply.append("- ")
                            .append(task.getTime() != null ? task.getTime() + " " : "")
                            .append(task.getTitle());

                    if (task.getDate() != null) {
                        reply.append(" (").append(task.getDate()).append(")");
                    }

                    reply.append("\n");
                }


                return new ChatResponse(reply.toString());
            } else {
                return new ChatResponse("Sorry, I can only tell you about your tasks for today.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("Sorry, I couldn’t fetch your tasks right now.");
        }
    }
}
