package com.himedia.luckydokiaiapi.domain.chatbot.service;

import com.himedia.luckydokiaiapi.domain.chatbot.domain.ChatMessage;
import com.himedia.luckydokiaiapi.domain.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage saveChatMessage(String question, String answer, 
                                      LocalDateTime questionTime, LocalDateTime answerTime) {
        ChatMessage chatMessage = ChatMessage.builder()
                .question(question)
                .answer(answer)
                .questionTime(questionTime)
                .answerTime(answerTime)
                .build();
        
        log.info("저장 중인 채팅 메시지: 질문={}, 질문 시간={}, 응답 시간={}", 
                question, questionTime, answerTime);
        
        return chatMessageRepository.save(chatMessage);
    }
} 