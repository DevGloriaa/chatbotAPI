package com.example.chatbotapi.serviceImpl;

import com.example.chatbotapi.dto.ChatResponse;
import com.example.chatbotapi.dto.HuggingFaceResponse;
import com.example.chatbotapi.service.ChatService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ChatServiceImpl implements ChatService {

    private final String apiKey;
    private final WebClient webClient;

    public ChatServiceImpl() {
        Dotenv dotenv = Dotenv.load();
        this.apiKey = dotenv.get("HF_API_KEY");

        this.webClient = WebClient.builder()
                .baseUrl("https://api-inference.huggingface.co/models/gpt2")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public ChatResponse getChatResponse(String message) {
        String requestBody = """
            {
                "inputs": "%s"
            }
        """.formatted(message);

        try {
            HuggingFaceResponse[] hfResponse = webClient.post()
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Accept", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(HuggingFaceResponse[].class)
                    .block();

            if (hfResponse != null && hfResponse.length > 0) {
                return new ChatResponse(hfResponse[0].getGeneratedText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ChatResponse("Sorry, I couldn’t generate a response.");
    }
}
