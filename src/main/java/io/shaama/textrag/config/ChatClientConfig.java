package io.shaama.textrag.config;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ChatClientConfig {

    private final VertexAiGeminiChatModel vertexAiGeminiChatModel;

    private final VectorStore vectorStore;

    @Bean
    public QuestionAnswerAdvisor questionAnswerAdvisor() {

        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(
                        SearchRequest.builder()
                                .similarityThreshold(0.8d)
                                .topK(5)
                                .build()
                )
                .build();
    }


    @Bean
    public ChatClient chatClient() {
        return ChatClient.create(vertexAiGeminiChatModel);
    }
}
