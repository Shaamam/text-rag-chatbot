package io.shaama.textrag.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;

    @Bean
    public ChatClient chatClient() {
        return ChatClient.create(vertexAiGeminiChatModel);
    }
}
