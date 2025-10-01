package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.dto.Task;
import com.example.chatbotapi.service.ChatService;
import com.example.chatbotapi.service.OptimusService;
import com.example.chatbotapi.serviceImpl.HuggingFaceService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final OptimusService optimusService;
    private final HuggingFaceService huggingFaceService;

    public ChatServiceImpl(OptimusService optimusService,
                           HuggingFaceService huggingFaceService) {
        this.optimusService = optimusService;
        this.huggingFaceService = huggingFaceService;
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
            }

            String hfReply = huggingFaceService.chatWithHuggingFace(message);
            return new ChatResponse(hfReply);

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatResponse("⚠️ Sorry, I couldn’t fetch a response right now.");
        }
    }
}
