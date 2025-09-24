package io.shaama.textrag.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@Slf4j
public class ChatService {

    private final ChatClient inMemoryChatClient;
    private final VectorStore vectorStore;

    public ChatService(VertexAiGeminiChatModel vertexAiGeminiChatModel, ChatMemory chatMemory, VectorStore vectorStore) {

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();
        this.vectorStore = vectorStore;
        inMemoryChatClient = ChatClient.builder(vertexAiGeminiChatModel)
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();
    }

    public ChatResponse getAnswer(ChatRequest chatRequest) {

        String question = chatRequest.Question();
        String sessionId = chatRequest.sessionId();

        String response = inMemoryChatClient
                .prompt()
                .system("You are a helpful assistant")
                .user(question)
                .advisors(a -> a.param(CONVERSATION_ID, sessionId))
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();

        return new ChatResponse(sessionId, question, response);

    }
}
