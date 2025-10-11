package com.example.chatbotapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ChatbotApiApplication {

    public static void main(String[] args) {

        SpringApplication.run(ChatbotApiApplication.class, args);
        System.out.println("ChatbotApiApplication started🎊");
    }

}
