package com.himedia.luckydokiaiapi.domain.chatbot.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Document(collection = "chat_message")
public class ChatMessage {
    
    @Id
    private String id;
    private String question;
    private String answer;
    private LocalDateTime questionTime;
    private LocalDateTime answerTime;
    
    @Builder
    public ChatMessage(String question, String answer, LocalDateTime questionTime, LocalDateTime answerTime) {
        this.question = question;
        this.answer = answer;
        this.questionTime = questionTime;
        this.answerTime = answerTime;
    }
} 