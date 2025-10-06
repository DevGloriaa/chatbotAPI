package com.example.chatbotapi.controller;

import com.example.chatbotapi.model.Memory;
import com.example.chatbotapi.service.MemoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    private final MemoryService memoryService;

    public MemoryController(MemoryService memoryService) {
        this.memoryService = memoryService;
    }

    @PostMapping("/save")
    public Memory saveMemory(@RequestBody Memory memory) {

        return memoryService.saveMemory(
                memory.getUserMessage(),
                memory.getBotResponse(),
                memory.getTopic(),
                memory.getEmail()
        );
    }


    @GetMapping("/all")
    public List<Memory> getAllMemories() {
        return memoryService.getAllMemories();
    }

    @PostMapping("/topic")
    public List<Memory> getMemoriesByTopic(@RequestBody Memory request) {
        return memoryService.getMemoriesByTopic(request.getTopic());
    }

    @PostMapping("/search")
    public List<Memory> searchMemories(@RequestBody Memory request) {
        return memoryService.searchMemories(request.getUserMessage());
    }

    @GetMapping("/by-email")
    public List<Memory> getMemoriesByEmail(@RequestParam String email) {
        return memoryService.getMemoriesByEmail(email);
    }
}
