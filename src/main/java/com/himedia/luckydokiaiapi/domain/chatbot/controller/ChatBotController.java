package com.himedia.luckydokiaiapi.domain.chatbot.controller;

import com.himedia.luckydokiaiapi.domain.chatbot.domain.ChatbotRoom;
import com.himedia.luckydokiaiapi.domain.chatbot.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ChatBotService chatBotService;

    @GetMapping("/ask")
    public Flux<String> recommendLuckidoki(
            @RequestParam String question,
            @RequestParam(required = false) String userEmail) throws Exception {
        log.info("Question received - userEmail: {}, question: {}", 
                userEmail != null ? userEmail : "anonymous", question);
        
        LocalDateTime questionTime = LocalDateTime.now();

        // 채팅방 찾기 또는 생성
        ChatbotRoom chatRoom = chatBotService.findOrCreateChatRoom(userEmail);
        String sessionId = chatRoom.getSessionId();

        // Fetch similar movies using vector store
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(question)
                        .withSimilarityThreshold(0.7)
                        .withTopK(5)
        );
        
        log.info("Found {} relevant documents", results.size());

        String template = """
                컨텍스트:
                {context}
                
                질문: 
                {question}
                
                답변:
                """;

        AtomicReference<StringBuilder> fullAnswerBuilder = new AtomicReference<>(new StringBuilder());
        AtomicReference<LocalDateTime> answerTimeRef = new AtomicReference<>(LocalDateTime.now());

        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(template)
                        .param("context", results)
                        .param("question", question))
                .stream()
                .content()
                .doOnNext(chunk -> {
                    fullAnswerBuilder.get().append(chunk);
                })
                .doOnComplete(() -> {
                    String fullAnswer = fullAnswerBuilder.get().toString();
                    log.info("응답 완료: {}", fullAnswer);
                    chatBotService.saveChatInteraction(
                            sessionId,
                            userEmail,
                            question, 
                            fullAnswer, 
                            questionTime, 
                            answerTimeRef.get()
                    );
                });
    }

    @PostMapping("/room/start")
    public ResponseEntity<String> startChatRoom(@RequestParam(required = false) String userEmail) {
        log.info("채팅방 생성 요청 - userEmail: {}", userEmail != null ? userEmail : "anonymous");
        ChatbotRoom chatRoom = chatBotService.createChatRoom(userEmail);
        return new ResponseEntity<>(chatRoom.getSessionId(), HttpStatus.CREATED);
    }

    @PostMapping("/room/close")
    public ResponseEntity<String> closeChatRoom(
            @RequestParam String sessionId,
            @RequestParam(required = false) String userEmail) {
        log.info("채팅방 종료 요청 - sessionId: {}, userEmail: {}", 
                sessionId, userEmail != null ? userEmail : "anonymous");
        String chatRoomId = chatBotService.closeChatRoom(sessionId, userEmail);
        return new ResponseEntity<>(chatRoomId, HttpStatus.OK);
    }
}
