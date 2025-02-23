package com.himedia.luckydokiaiapi.domain.report.service;

import com.himedia.luckydokiaiapi.domain.ai.service.OpenAiService;
import com.himedia.luckydokiaiapi.domain.report.dto.MemberMetricsResponse;
import com.himedia.luckydokiaiapi.domain.report.dto.ProductMetricsResponse;
import com.himedia.luckydokiaiapi.domain.report.dto.ReportGenerationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {
    private final OpenAiService openAiService;
    private final PdfGeneratorService pdfGeneratorService;

    public String generateMonthlyReport(ReportGenerationRequest request) {
        log.info("generateMonthlyReport start...");
        // 1. OpenAI API를 통한 리포트 텍스트 생성
        String aiReportContent = openAiService.call(this.createPromptFromMetrics(request));
        log.info("AI Report Content: success! ");
        // 2. PDF 생성
        return pdfGeneratorService.generatePdfReport(aiReportContent, request);
    }


    private String createPromptFromMetrics(ReportGenerationRequest request) {
        return String.format("""
                Please create a detailed monthly business report in Korean language with the following metrics:
                
                기간: %s ~ %s
                
                1. 고객 성장:
                - 월간 신규 회원: %d명
                - 월간 신규 셀러: %d명 (전체 신규 회원 대비 %.1f%%)
                
                2. 매출 실적:
                - 월간 주문 수: %d건
                - 월간 매출액: ₩%d
                - 일평균 매출액: ₩%d
                - 당일 매출액: ₩%d
                
                3. 인기 상품 TOP 10 (평가 기준: 리뷰평점 × 2 + 리뷰수 + 좋아요수 + 주문수):
                %s
                
                4. 커뮤니티 참여:
                - 총 게시글: %d개
                
                5. 우수 회원:
                - TOP 5 판매자 (좋아요수 + 판매량 기준): %s
                - TOP 5 우수 구매자 (구매액 + 10자 이상 리뷰 작성): %s
                
                Please provide a comprehensive analysis in Korean with the following structure:
                
                1. 주요 요약
                - 전반적인 비즈니스 성과 요약
                - 주목할만한 성장 지표와 개선이 필요한 영역
                
                2. 상세 분석
                2.1 회원 분석
                - 신규 회원 증감 추이와 셀러 비율 분석
                - 우수 회원들의 특징과 행동 패턴
                
                2.2 매출 분석
                - 주문 및 매출 추이
                - 일평균 매출 대비 당일 매출 비교
                - 매출 기여도가 높은 상품 카테고리
                
                2.3 상품 분석
                - TOP 10 상품들의 공통적 특징
                - 높은 평가를 받은 요인 분석
                - 가격대별 인기 상품 분포
                
                2.4 커뮤니티 활성화 분석
                - 게시글 참여도와 회원 활동성
                - 우수 구매자들의 리뷰 작성 패턴
                
                3. 인사이트 및 제안사항
                3.1 성장 기회
                - 데이터 기반 타겟 고객층 제안
                - 신규 셀러 유치 전략
                - 우수 회원 특성을 활용한 마케팅 방안
                
                3.2 개선 필요 영역
                - 매출 증대를 위한 구체적 액션 아이템
                - 커뮤니티 활성화 방안
                - 리뷰 작성 독려 전략
                
                3.3 중점 추진 과제
                - 단기 (1개월) 및 중기 (3개월) 중점 과제
                - 우선순위별 실행 계획
                
                Note: Please write the entire report in Korean language with data-driven insights.
                """,
                request.getStartDate(), request.getEndDate(),
                request.getMetrics().getNewMemberCount(),
                request.getMetrics().getNewSellerCount(),
                (float) request.getMetrics().getNewSellerCount() / request.getMetrics().getNewMemberCount() * 100,
                request.getMetrics().getTotalOrderCount(),
                request.getMetrics().getMonthlyRevenue(),
                request.getMetrics().getMonthlyRevenue() / 30,  // 일평균 매출액 계산
                request.getMetrics().getTodayRevenue(),
                formatTop10Products(request.getMetrics().getTop10Products()),
                request.getMetrics().getTotalCommunityCount(),
                formatTopSellers(request.getMetrics().getTop5Sellers()),
                formatTopConsumers(request.getMetrics().getTop5GoodConsumers())
        );
    }

    private String formatTop10Products(List<ProductMetricsResponse> products) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < products.size(); i++) {
            ProductMetricsResponse p = products.get(i);
            sb.append(String.format("%d. %s (카테고리: %s, 가격: ₩%d, 판매량: %d, 평점: %.1f, 리뷰: %d개)\n",
                    i + 1, p.getCategoryAllName(), p.getName(), p.getDiscountPrice(), p.getSalesCount(),
                    p.getReviewAverage(), p.getReviewCount()));
        }
        return sb.toString();
    }

    private String formatTopConsumers(List<MemberMetricsResponse> members) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            MemberMetricsResponse m = members.get(i);
            sb.append(String.format("%d. %s (월간 구매액: ₩%d, 리뷰 작성: %d개)\n",
                    i + 1, m.getNickName(), m.getMonthlyPurchase(), m.getReviewCount()));
        }
        return sb.toString();
    }

    private String formatTopSellers(List<MemberMetricsResponse> members) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < members.size(); i++) {
            MemberMetricsResponse m = members.get(i);
            sb.append(String.format("%d. %s (월간 판매액: ₩%d)\n",
                    i + 1, m.getNickName(),
                    m.getMonthlySales() != 0 ? m.getMonthlySales() : 0));
        }
        return sb.toString();
    }
}
