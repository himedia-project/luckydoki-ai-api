package com.himedia.luckydokiaiapi.domain.chatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAiService {

    @Value("${spring.ai.openai.chat.options.model}")
    private String openAiModelName;

    private final ChatClient chatClient;

    private final ChatModel chatModel;


    /**
     * 챗봇 openAI chatModel 호출
     * @param message 사용자 메시지
     * @return 챗봇 응답 메시지
     */
    public String call(String message) {
        ChatResponse response = chatModel.call(
                new Prompt(
                        message,
                        OpenAiChatOptions.builder()
                                .withModel(openAiModelName)        // default: gpt-4o, 그외: gpt-4, gpt-4-turbo, gpt-4-mini, gpt-3.5-turbo
                                .withTemperature(0.4)       // temperature란? 0.0 ~ 1.0 사이의 값으로, 높을수록 더 무작위적인 답변을 생성
                                .build()
                )
        );
        return response.getResult().getOutput().getContent();
    }

}
