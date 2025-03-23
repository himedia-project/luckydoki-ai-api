package com.himedia.luckydokiaiapi.domain.chatbot.repository;

import com.himedia.luckydokiaiapi.domain.chatbot.domain.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    // 기본 CRUD 기능 외에 필요한 메소드가 있다면 여기에 추가할 수 있습니다.
} 