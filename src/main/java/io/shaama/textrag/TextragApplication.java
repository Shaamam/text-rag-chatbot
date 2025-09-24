package io.shaama.textrag;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TextragApplication {

    public static void main(String[] args) {
        SpringApplication.run(TextragApplication.class, args);
    }

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder().build();
    }

}
