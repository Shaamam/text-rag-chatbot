package io.shaama.textrag.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chat")
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest chatRequest) {

        try {
            log.info("Received question: {}", chatRequest.Question());
            ChatResponse response = chatService.getAnswer(chatRequest);
            log.info("Generated response: {}", response.answer());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing question: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
