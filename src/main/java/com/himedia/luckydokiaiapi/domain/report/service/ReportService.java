package com.himedia.luckydokiaiapi.domain.report.service;

import com.himedia.luckydokiaiapi.domain.ai.service.OpenAiService;
import com.himedia.luckydokiaiapi.domain.report.dto.ReportGenerationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    private final OpenAiService openAiService;
    private final PdfGeneratorService pdfGeneratorService;

    public String generateMonthlyReport(ReportGenerationRequest request) {
        // 1. OpenAI API를 통한 리포트 텍스트 생성
        String aiReportContent = openAiService.call(this.createPromptFromMetrics(request));

        // 2. PDF 생성
        return pdfGeneratorService.generatePdfReport(aiReportContent, request);
    }


    private String createPromptFromMetrics(ReportGenerationRequest request) {
        return String.format("""
                        Please create a detailed monthly business report in Korean language with the following metrics:
                        
                        기간: %s ~ %s
                        
                        1. 고객 성장:
                        - 신규 회원: %d명
                        
                        2. 매출 실적:
                        - 총 주문 수: %d건
                        - 매출액: ₩%d
                        
                        3. 인기 상품 TOP 10:
                        %s
                        
                        4. 커뮤니티 참여:
                        - 총 게시글: %d개
                        
                        5. 우수 회원:
                        - TOP 5 판매자: %s
                        - TOP 5 구매자: %s
                        
                        Please analyze these metrics and provide in Korean:
                        1. 주요 요약
                        2. 각 항목별 상세 분석
                        3. 주요 인사이트 및 제안사항
                        
                        Note: Please write the entire report in Korean language.
                        """,
                request.getStartDate(), request.getEndDate(),
                request.getMetrics().getNewMemberCount(),
                request.getMetrics().getTotalOrderCount(),
                request.getMetrics().getTodayRevenue(),
                request.getMetrics().getTop10Products(),
                request.getMetrics().getTotalCommunityCount(),
                request.getMetrics().getTop5Sellers(),
                request.getMetrics().getTop5GoodConsumers()
        );
    }
}
