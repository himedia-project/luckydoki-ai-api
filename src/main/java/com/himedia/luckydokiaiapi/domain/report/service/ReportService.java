package com.himedia.luckydokiaiapi.domain.report.service;

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
    //    private final OpenAiService openAiService;
    private final PdfGeneratorService pdfGeneratorService;

    public String generateMonthlyReport(ReportGenerationRequest request) {
        // 1. OpenAI API를 통한 리포트 텍스트 생성
//        String reportContent = generateReportContent(request);
        String reportContent = createPromptFromMetrics(request);

        // 2. PDF 생성
        return pdfGeneratorService.generatePdfReport(reportContent, request);
    }

//    private String generateReportContent(ReportGenerationRequest request) {
//        String prompt = createPromptFromMetrics(request);
//
//        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
//                .messages(Arrays.asList(
//                        new ChatMessage("system", "You are a professional business analyst. Create a detailed monthly report in Korean."),
//                        new ChatMessage("user", prompt)
//                ))
//                .build();
//
//        return openAiService.createChatCompletion(chatRequest)
//                .getChoices().get(0).getMessage().getContent();
//    }

    private String createPromptFromMetrics(ReportGenerationRequest request) {
        return String.format("""
            Create a detailed monthly business report with the following metrics:
            
            Period: %s ~ %s
            
            1. Customer Growth:
            - New Members: %d
            
            2. Sales Performance:
            - Total Orders: %d
            - Revenue: ₩%d
            
            3. Top 10 Products:
            %s
            
            4. Community Engagement:
            - Total Posts: %d
            
            5. Top Performers:
            - Top 5 Sellers: %s
            - Top 5 Consumers: %s
            
            Please analyze these metrics and provide:
            1. Executive Summary
            2. Detailed Analysis for each category
            3. Key Insights and Recommendations
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
