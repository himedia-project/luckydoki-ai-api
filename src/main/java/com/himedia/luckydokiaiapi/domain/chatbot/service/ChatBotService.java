package com.himedia.luckydokiaiapi.domain.chatbot.service;

import com.himedia.luckydokiaiapi.domain.chatbot.domain.ChatbotMessage;
import com.himedia.luckydokiaiapi.domain.chatbot.domain.ChatbotRoom;
import com.himedia.luckydokiaiapi.domain.chatbot.repository.ChatbotMessageRepository;
import com.himedia.luckydokiaiapi.domain.chatbot.repository.ChatbotRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final ChatbotMessageRepository chatbotMessageRepository;
    private final ChatbotRoomRepository chatbotRoomRepository;
    private static final String CHATBOT_EMAIL = "chatbot";

    public void saveChatInteraction(String sessionId, String userEmail, String question, String answer, 
                                  LocalDateTime questionTime, LocalDateTime answerTime) {
        // 채팅방 찾기
        ChatbotRoom chatRoom;
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            chatRoom = chatbotRoomRepository.findByUserEmailAndActiveTrue(userEmail)
                    .orElseThrow(() -> new RuntimeException("활성화된 채팅방을 찾을 수 없습니다."));
        } else {
            chatRoom = chatbotRoomRepository.findBySessionIdAndActiveTrue(sessionId)
                    .orElseThrow(() -> new RuntimeException("활성화된 채팅방을 찾을 수 없습니다."));
        }

        // 사용자 질문 저장
        ChatbotMessage userMessage = ChatbotMessage.builder()
                .chatRoomId(chatRoom.getId())
                .email(userEmail != null && !userEmail.trim().isEmpty() ? userEmail : "anonymous")
                .message(question)
                .timestamp(questionTime)
                .build();
        
        chatbotMessageRepository.save(userMessage);

        // 챗봇 응답 저장
        ChatbotMessage botMessage = ChatbotMessage.builder()
                .chatRoomId(chatRoom.getId())
                .email(CHATBOT_EMAIL)
                .message(answer)
                .timestamp(answerTime)
                .build();
        
        chatbotMessageRepository.save(botMessage);

        // 채팅방 마지막 상호작용 정보 업데이트
        // 마지막 메시지는 사용자의 질문과 챗봇의 답변을 함께 저장
        String lastMessage = String.format("Q: %s\nA: %s", question, answer);
        chatRoom.updateLastInteraction(lastMessage, answerTime);
        chatbotRoomRepository.save(chatRoom);
        
        log.info("채팅 내역 저장 완료 - 채팅방: {}, 마지막 메시지 시간: {}", chatRoom.getId(), answerTime);
    }

    // 채팅방 생성
    public ChatbotRoom createChatRoom(String userEmail) {
        ChatbotRoom newRoom = ChatbotRoom.builder()
                .userEmail(userEmail)
                .build();
        
        ChatbotRoom savedRoom = chatbotRoomRepository.save(newRoom);
        log.info("새로운 채팅방 생성 완료 - roomId: {}, sessionId: {}, userEmail: {}", 
                savedRoom.getId(), savedRoom.getSessionId(), 
                savedRoom.isAnonymous() ? "anonymous" : savedRoom.getUserEmail());
        
        return savedRoom;
    }

    // 채팅방 종료
    public String closeChatRoom(String sessionId, String userEmail) {
        log.info("closeChatRoom - sessionId: {}, userEmail: {}", sessionId, userEmail);
        ChatbotRoom chatRoom;
        
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            chatRoom = chatbotRoomRepository.findByUserEmailAndActiveTrue(userEmail)
                    .orElseThrow(() -> new RuntimeException("활성화된 채팅방을 찾을 수 없습니다."));
        } else {
            chatRoom = chatbotRoomRepository.findBySessionIdAndActiveTrue(sessionId)
                    .orElseThrow(() -> new RuntimeException("활성화된 채팅방을 찾을 수 없습니다."));
        }
        
        chatRoom.deactivate();
        chatbotRoomRepository.save(chatRoom);
        log.info("채팅방 종료 - roomId: {}, sessionId: {}, userEmail: {}", 
                chatRoom.getId(), chatRoom.getSessionId(), 
                chatRoom.isAnonymous() ? "anonymous" : chatRoom.getUserEmail());
        return chatRoom.getId();
    }

    public ChatbotRoom findOrCreateChatRoom(String userEmail) {
        ChatbotRoom chatRoom;
        
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            // 로그인 사용자의 경우 기존 활성화된 채팅방 찾기
            chatRoom = chatbotRoomRepository.findByUserEmailAndActiveTrue(userEmail)
                    .orElseGet(() -> createChatRoom(userEmail));
        } else {
            // 비로그인 사용자의 경우 항상 새로운 채팅방 생성
            chatRoom = createChatRoom(null);
        }
        
        return chatRoom;
    }
} 