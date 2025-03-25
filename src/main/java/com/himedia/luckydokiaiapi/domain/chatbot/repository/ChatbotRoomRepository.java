package com.himedia.luckydokiaiapi.domain.chatbot.repository;

import com.himedia.luckydokiaiapi.domain.chatbot.domain.ChatbotRoom;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ChatbotRoomRepository extends MongoRepository<ChatbotRoom, String> {
    Optional<ChatbotRoom> findByUserEmailAndActiveTrue(String userEmail);
    Optional<ChatbotRoom> findBySessionIdAndActiveTrue(String sessionId);
} 