package com.himedia.luckydokiaiapi.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    final String prompt = """
            '안녕하세요'라고 말하면 '안녕하세요'라고 대답하는 학습도우미입니다.
            친절하게 대답하는 학습도우미입니다.
            """;

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder){
        return chatClientBuilder
                .defaultSystem(prompt)  // 학습도우미
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory())).build();
    }

}
