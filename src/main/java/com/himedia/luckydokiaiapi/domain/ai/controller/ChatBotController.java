package com.himedia.luckydokiaiapi.domain.ai.controller;


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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    @GetMapping("/ask")
    public Flux<String> recommendLuckidoki(@RequestParam("question") String question) throws Exception {

        System.out.println(question);
        // Fetch similar movies using vector store
        List<Document> results = vectorStore.similaritySearch(SearchRequest.query(question).withSimilarityThreshold(0.5).withTopK(1));
        // vectorStore.similaritySearch(): 벡터 저장소에서 유사도 기반 검색을 수행
        // SearchRequest.query(question): 사용자가 입력한 질문을 검색 쿼리로 사용
        // 유사도 임계값을 0.5로 설정
        // withTopK(1): 가장 유사한 상위 1개의 결과만 반환하도록 설정합니다.
        // 만약 .withTopK(5)로 설정하면 상위 5개의 결과를 반환
        // 결과는 List<Document>로 반환되며, 각 Document는 검색된 문서의 내용과 메타데이터를 포함

        System.out.println(results.size());

        // Flux 는 Byte Stream을 처리하는데 사용되는 리액티브 API
        // Flux : 0개 또는 여러 개의 결과를 비동기적으로 반환 -> 유저는 실시간으로 결과를 받아볼 수 있음

        String template = """
                당신은 '럭키도키' 쇼핑몰의 AI 상담사입니다. 
                항상 친절하고 전문적으로 응대해야 하며, 다음 가이드라인을 따라주세요:
                
                1. 기본 응대 방식:
                - 고객을 '고객님'으로 호칭
                - 정중하고 공손한 말투 사용
                - 모르는 내용에 대해서는 "죄송합니다만, 해당 내용은 확인이 어렵습니다." 라고 답변
                
                2. 상품 문의 응대:
                - 상품의 가격, 할인 정보, 재고 상태 등 정확한 정보 제공
                - 상품 특징과 장점 설명
                - 상품 평가(리뷰, 평점) 정보 제공
                - 상품 추천시 이미지와 상품 페이지 링크 함께 제공 (형식: 상품 이미지: [이미지URL], 상품 상세 페이지: localhost:3000/product/[상품ID])
                
                3. 셀러 정보 안내:
                - 셀러의 전문성과 신뢰도 강조
                - 셀러의 대표 상품들 소개
                - 셀러 정보 제공시 프로필 이미지와 셀러 페이지 링크 포함 (형식: 셀러 이미지: [이미지URL], 셀러 페이지: localhost:3000/shop/[셀러ID])
                
                4. 추천 서비스:
                - 고객의 관심사와 예산을 고려한 맞춤 추천
                - 비슷한 카테고리의 인기 상품 추천
                - 할인/이벤트 상품 우선 추천
                - 추천시 항상 이미지와 링크 포함
                
                5. 특별 안내:
                - 신상품은 "NEW" 태그로 강조
                - 베스트상품은 "BEST" 태그로 강조
                - 이벤트 상품은 "EVENT" 태그와 함께 할인율 강조
                
                컨텍스트:
                {context}
                
                질문: 
                {question}
                
                답변:
                """;

        return chatClient.prompt()
                .user(promptUserSpec -> promptUserSpec.text(template)
                        .param("context", results)
                        .param("question", question))
                .stream()
                .content();
    }

}
