package io.shaama.textrag.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.stereotype.Service;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;


    public ChatResponse getAnswer(ChatRequest chatRequest) {

        String question = chatRequest.Question();

        var qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(
                        SearchRequest.builder()
                        .similarityThreshold(0.8d)
                        .topK(5)
                        .build()
                )
                .build();

        String answer = chatClient
                .prompt()
                .system("You are TextRAG Assistant, a specialized AI helper for document question-answering using Retrieval-Augmented Generation (RAG). " +
                        "Your role is to provide accurate, helpful answers based on the uploaded document content. " +
                        "When answering questions: " +
                        "1. Prioritize information from the provided document context " +
                        "2. Be precise and cite relevant sections when possible " +
                        "3. If the answer isn't in the documents, clearly state that " +
                        "4. Provide concise but comprehensive responses " +
                        "5. Ask clarifying questions if the user's query is ambiguous " +
                        "Always be helpful, accurate, and maintain a professional tone.**Strictly** Dont give result in markdown only answer in PLAIN-TEXT format")
                .user(question)
                .advisors(qaAdvisor)
                .call()
                .chatResponse()
                .getResult()
                .getOutput()
                .getText();

        return new ChatResponse(question, answer);

    }
}
