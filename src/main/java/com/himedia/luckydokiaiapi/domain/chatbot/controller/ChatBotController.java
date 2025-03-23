package com.himedia.luckydokiaiapi.domain.chatbot.controller;

import com.himedia.luckydokiaiapi.domain.chatbot.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final ChatMessageService chatMessageService;

    @GetMapping("/ask")
    public Flux<String> recommendLuckidoki(@RequestParam("question") String question) throws Exception {
        log.info("Question received: {}", question);
        
        // 질문 시간 기록
        LocalDateTime questionTime = LocalDateTime.now();

        // Fetch similar movies using vector store
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(question)
                        .withSimilarityThreshold(0.7)
                        .withTopK(5)
        );
        // vectorStore.similaritySearch(): 벡터 저장소에서 유사도 기반 검색을 수행
        // SearchRequest.query(question): 사용자가 입력한 질문을 검색 쿼리로 사용
        // 유사도 임계값을 0.5로 설정, 유사도 임계값을 1로 설정하면 정확히 일치하는 결과만 반환
        // withTopK(1): 가장 유사한 상위 1개의 결과만 반환하도록 설정합니다.
        // 만약 .withTopK(5)로 설정하면 상위 5개의 결과를 반환
        // 결과는 List<Document>로 반환되며, 각 Document는 검색된 문서의 내용과 메타데이터를 포함

        log.info("Found {} relevant documents", results.size());

        // Flux 는 Byte Stream을 처리하는데 사용되는 리액티브 API
        // Flux : 0개 또는 여러 개의 결과를 비동기적으로 반환 -> 유저는 실시간으로 결과를 받아볼 수 있음

        String template = """
                컨텍스트:
                {context}
                
                질문: 
                {question}
                
                답변:
                """;

        // 전체 응답을 저장하기 위한 변수
        AtomicReference<StringBuilder> fullAnswerBuilder = new AtomicReference<>(new StringBuilder());
        
        // 응답 시작 시간 기록
        AtomicReference<LocalDateTime> answerTimeRef = new AtomicReference<>(LocalDateTime.now());

        // 응답 스트림 처리 및 MongoDB에 저장
        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(template)
                        .param("context", results)
                        .param("question", question))
                .stream()
                .content()
                .doOnNext(chunk -> {
                    // 응답 청크를 누적
                    fullAnswerBuilder.get().append(chunk);
                })
                .doOnComplete(() -> {
                    // 스트림이 완료되면 MongoDB에 저장
                    String fullAnswer = fullAnswerBuilder.get().toString();
                    log.info("응답 완료: {}", fullAnswer);
                    chatMessageService.saveChatMessage(
                            question, 
                            fullAnswer, 
                            questionTime, 
                            answerTimeRef.get()
                    );
                });
    }
}
