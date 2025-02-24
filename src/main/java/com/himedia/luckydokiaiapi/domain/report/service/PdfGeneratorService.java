package com.himedia.luckydokiaiapi.domain.report.service;


import com.himedia.luckydokiaiapi.domain.report.dto.*;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@Transactional
public class PdfGeneratorService {

    @Value("${upload.path}")
    private String uploads;

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(0, 222, 144);
    private static final String FONT_PATH = "static/fonts/NanumGothic.ttf";
    private PdfFont koreanFont;  // 폰트를 멤버 변수로 선언

    @PostConstruct
    public void init() {
        try {
            // 디렉토리 생성
            File directory = new File(uploads);
            if (!directory.exists()) {
                directory.mkdirs();
                log.info("리포트 저장 디렉토리 생성: {}", uploads);
            }

            // 폰트 초기화
            try (var fontStream = getClass().getClassLoader().getResourceAsStream(FONT_PATH)) {
                if (fontStream == null) {
                    throw new RuntimeException("폰트 파일을 찾을 수 없습니다: " + FONT_PATH);
                }
                byte[] fontBytes = fontStream.readAllBytes();
                koreanFont = PdfFontFactory.createFont(fontBytes, "Identity-H");
                log.info("한글 폰트 로드 완료");
            }
        } catch (Exception e) {
            log.error("초기화 실패", e);
            throw new RuntimeException("서비스 초기화 실패", e);
        }
    }

    public String generatePdfReport(String aiAnalysis, ReportGenerationRequest request) {
        log.info("PDF 리포트 생성 요청 start...");
        String fileName = String.format("monthly-ai-report-%s.pdf",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        String filePath = uploads + File.separator + fileName;

        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 폰트 설정
//            PdfFont font = PdfFontFactory.createFont(FONT_PATH, "Identity-H");
            document.setFont( // 미리 로드된 한글 폰트 사용
                    koreanFont
            );

            // 헤더 추가
            addHeader(document, request.getStartDate(), request.getEndDate());

            // 주요 지표 요약
            addMetricsSummary(document, request.getMetrics());

            // AI 분석 결과
            addAiAnalysis(document, aiAnalysis);

            // 상세 데이터 테이블
            addDetailedTables(document, request.getMetrics());

            document.close();
            return filePath;

        } catch (IOException e) {
            log.error("PDF 생성 중 오류 발생", e);
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    private void addHeader(Document document, LocalDate startDate, LocalDate endDate) {
        document.add(new Paragraph("월간 리포트")
                .setFont(koreanFont)
                .setFontSize(24)
                .setFontColor(PRIMARY_COLOR)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
//                .setMarginBottom(20);

        Paragraph period = new Paragraph(String.format("기간: %s ~ %s",
                startDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
                endDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);

//        document.add(header);
        document.add(period);
    }

    private void addMetricsSummary(Document document, DashboardMetrics metrics) {
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();

        // 주요 지표 추가
        addMetricCell(table, "신규 회원", metrics.getNewMemberCount() + "명");
        addMetricCell(table, "신규 셀러", metrics.getNewSellerCount() + "명");
        addMetricCell(table, "총 주문", metrics.getTotalOrderCount() + "건");
        addMetricCell(table, "매출", "₩" + String.format("%,d", metrics.getMonthlyRevenue()));
        addMetricCell(table, "커뮤니티 게시글", metrics.getTotalCommunityCount() + "개");

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addMetricCell(Table table, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label))
                .setFontSize(11)
                .setFontColor(ColorConstants.GRAY);

        Cell valueCell = new Cell()
                .add(new Paragraph(value))
                .setFontSize(14)
                .setBold()
                .setFontColor(PRIMARY_COLOR);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addAiAnalysis(Document document, String aiAnalysis) {
        document.add(new Paragraph("AI 분석 리포트")
                .setFontSize(18)
                .setFontColor(PRIMARY_COLOR)
                .setBold()
                .setMarginTop(30)
                .setMarginBottom(20));

        // AI 분석 내용을 섹션별로 분리하여 추가
        String[] sections = aiAnalysis.split("\n\n");
        for (String section : sections) {
            if (!section.trim().isEmpty()) {
                document.add(new Paragraph(section)
                        .setFontSize(11)
                        .setMarginBottom(10));
            }
        }
    }

    private void addDetailedTables(Document document, DashboardMetrics metrics) {
        // Top 10 Products Table
        document.add(new Paragraph("인기 상품 TOP 10")
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR)
                .setBold()
                .setMarginTop(30)
                .setMarginBottom(10));

        Table productsTable = createProductsTable(metrics.getTop10Products());
        document.add(productsTable);

        // Top Sellers & Consumers Tables
        document.add(new Paragraph("TOP 5 판매자 & 구매자")
                .setFontSize(16)
                .setFontColor(PRIMARY_COLOR)
                .setBold()
                .setMarginTop(30)
                .setMarginBottom(10));

        Table performersTable = createPerformersTable(
                metrics.getTop5Sellers(),
                metrics.getTop5GoodConsumers()
        );
        document.add(performersTable);
    }

    // 내부 테이블 Header 메소드 추가
    private void addTableHeader(Table table, String[] headers) {
        for (String header : headers) {
            table.addCell(
                    new Cell()
                            .add(new Paragraph(header).setFont(koreanFont))
                            .setFontColor(ColorConstants.GRAY)
                            .setBackgroundColor(new DeviceRgb(245, 245, 245))
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setFontSize(10)
            );
        }
    }


    // 상품 테이블 생성 메소드
    private Table createProductsTable(List<ProductMetricsResponse> products) {
        // 컬럼 비율 조정 (총 합 100%)
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 15, 25, 10, 15, 10, 10, 10}))
                .useAllAvailableWidth();

        // 테이블 헤더
        addTableHeader(table, new String[]{"순위", "카테고리", "상품명", "셀러", "가격", "판매량", "평점", "리뷰수"});

        // 데이터 행 추가
        for (int i = 0; i < products.size(); i++) {
            ProductMetricsResponse product = products.get(i);
            table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getCategoryAllName()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getName()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getShopName()))));
            table.addCell(new Cell().add(new Paragraph("₩" + String.format("%,d", product.getDiscountPrice()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getSalesCount()))));
            table.addCell(new Cell().add(new Paragraph(String.format("%.1f", product.getReviewAverage()))));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(product.getReviewCount()))));
        }

        // 테이블 스타일링
        table.setFontSize(9)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10)
                .setMarginBottom(20);

        return table;
    }


    private Table createPerformersTable(List<MemberMetricsResponse> sellers, List<MemberMetricsResponse> consumers) {
        // 6컬럼: 순위 | 판매자 | 판매실적 | 순위 | 구매자 | 구매실적
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 25, 20, 5, 25, 20}))
                .useAllAvailableWidth();

        // 테이블 헤더 (병합된 셀로 구성)
        Cell sellerHeader = new Cell(1, 3)  // 1행 3열 병합
                .add(new Paragraph("판매자 TOP 5"))
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PRIMARY_COLOR)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);

        Cell consumerHeader = new Cell(1, 3)  // 1행 3열 병합
                .add(new Paragraph("구매자 TOP 5"))
                .setFontColor(ColorConstants.WHITE)
                .setBackgroundColor(PRIMARY_COLOR)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);

        table.addHeaderCell(sellerHeader);
        table.addHeaderCell(consumerHeader);

        // 서브 헤더 추가
        addTableHeader(table, new String[]{"순위", "닉네임", "판매실적", "순위", "닉네임", "구매실적"});

        // 데이터 행 추가
        for (int i = 0; i < 5; i++) {
            // 판매자 정보
            if (i < sellers.size()) {
                MemberMetricsResponse seller = sellers.get(i);
                table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(seller.getNickName() != null ? seller.getNickName() : "-"))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format("₩%,d", seller.getMonthlySales())))
                        .setTextAlignment(TextAlignment.RIGHT));
            } else {
                // 데이터가 없는 경우 빈 셀
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setTextAlignment(TextAlignment.CENTER));

            }

            // 구매자 정보
            if (i < consumers.size()) {
                MemberMetricsResponse consumer = consumers.get(i);
                table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1)))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(consumer.getNickName() != null ? consumer.getNickName() : "-"))
                        .setTextAlignment(TextAlignment.CENTER));

                // 구매자 실적 정보 (구매액과 리뷰 수 표시)
                int purchase = consumer.getMonthlyPurchase();
                int reviews = consumer.getReviewCount();
                String performance = String.format("₩%,d\n(%d개 리뷰)", purchase, reviews);

                table.addCell(new Cell().add(new Paragraph(performance))
                        .setTextAlignment(TextAlignment.RIGHT));
            } else {
                // 데이터가 없는 경우 빈 셀
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-"))
                        .setTextAlignment(TextAlignment.CENTER));
            }
        }

        // 테이블 스타일링
        table.setFontSize(10)
                .setMarginTop(10)
                .setMarginBottom(20);

        return table;
    }
}
