package com.himedia.luckydokiaiapi.domain.report.service;

import com.himedia.luckydokiaiapi.domain.report.dto.DashboardMetrics;
import com.himedia.luckydokiaiapi.domain.report.dto.MemberMetricsResponse;
import com.himedia.luckydokiaiapi.domain.report.dto.ProductMetricsResponse;
import com.himedia.luckydokiaiapi.domain.report.dto.ReportGenerationRequest;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class PdfGeneratorService {

    @Value("${upload.path}")
    private String uploads;

    private static final DeviceRgb PRIMARY_COLOR = new DeviceRgb(0, 222, 144);
    private static final String FONT_PATH = "static/fonts/NanumGothic.ttf";

    // 각 PDF 생성 시마다 새 PdfFont 인스턴스를 생성하기 위한 메서드
    private PdfFont createLocalKoreanFont() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/" + FONT_PATH)) {
            if (is == null) {
                throw new IOException("폰트 리소스를 찾을 수 없습니다: " + FONT_PATH);
            }
            byte[] fontBytes = is.readAllBytes();

            // 1) FontProgram 생성
            FontProgram fontProgram = FontProgramFactory.createFont(fontBytes);

            // 2) PdfFont 생성 (인코딩: IDENTITY_H)
            return PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H);
        }
    }

    public String generatePdfReport(String aiAnalysis, ReportGenerationRequest request, Map<String, Object> salesGraphResult) {
        log.info("PDF 리포트 생성 요청 start...");
        // 고유 파일명 생성 (날짜 + 밀리초)
        String fileName = String.format("monthly-ai-report-%s.pdf",
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        String filePath = uploads + File.separator + fileName;

        try {
            PdfWriter writer = new PdfWriter(filePath);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 매번 새 PdfFont 인스턴스 생성
            PdfFont localKoreanFont = createLocalKoreanFont();
            document.setFont(localKoreanFont);

            // 헤더 추가
            addHeader(document, request.getStartDate(), request.getEndDate(), localKoreanFont);

            // 주요 지표 요약 추가
            addMetricsSummary(document, request.getMetrics(), localKoreanFont);

            // AI 분석 결과 추가
            addAiAnalysis(document, aiAnalysis);

            // 상세 데이터 테이블 추가
            addDetailedTables(document, request.getMetrics(), localKoreanFont);

            // 판매 그래프 추가
            addSalesGraphs(document, salesGraphResult);

            document.close();
            return filePath;
        } catch (IOException e) {
            log.error("PDF 생성 중 오류 발생", e);
            throw new RuntimeException("PDF 생성 실패", e);
        }
    }

    private void addHeader(Document document, LocalDate startDate, LocalDate endDate, PdfFont font) {
        Paragraph title = new Paragraph("월간 리포트")
            .setFont(font)
            .setFontSize(24)
            .setFontColor(PRIMARY_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        Paragraph period = new Paragraph(String.format("기간: %s ~ %s",
            startDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")),
            endDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))))
            .setFontSize(12)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginBottom(30);
        document.add(period);
    }

    private void addMetricsSummary(Document document, DashboardMetrics metrics, PdfFont font) {
        Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
        addMetricCell(table, "신규 회원", metrics.getNewMemberCount() + "명");
        addMetricCell(table, "신규 셀러", metrics.getNewSellerCount() + "명");
        addMetricCell(table, "총 주문", metrics.getTotalOrderCount() + "건");
        addMetricCell(table, "매출", "₩" + String.format("%,d", metrics.getMonthlyRevenue()));
        addMetricCell(table, "커뮤니티 게시글", metrics.getTotalCommunityCount() + "개");
        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void addMetricCell(Table table, String label, String value) {
        Cell labelCell = new Cell().add(new Paragraph(label))
            .setFontSize(11)
            .setFontColor(ColorConstants.GRAY);
        Cell valueCell = new Cell().add(new Paragraph(value))
            .setFontSize(14)
            .setBold()
            .setFontColor(PRIMARY_COLOR);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addAiAnalysis(Document document, String aiAnalysis) {
        Paragraph analysisTitle = new Paragraph("AI 분석 리포트")
            .setFontSize(18)
            .setFontColor(PRIMARY_COLOR)
            .setBold()
            .setMarginTop(30)
            .setMarginBottom(20);
        document.add(analysisTitle);
        String[] sections = aiAnalysis.split("\n\n");
        for (String section : sections) {
            if (!section.trim().isEmpty()) {
                Paragraph p = new Paragraph(section)
                    .setFontSize(11)
                    .setMarginBottom(10);
                document.add(p);
            }
        }
    }

    private static final float IMAGE_MAX_WIDTH = 500f;
    private static final float IMAGE_MAX_HEIGHT = 300f;

    private void addSalesGraphs(Document document, Map<String, Object> salesGraphResult) {
        if (salesGraphResult != null) {
            String dailyImageBase64 = (String) salesGraphResult.get("daily_image_base64");
            String hourlyImageBase64 = (String) salesGraphResult.get("hourly_image_base64");

            if (dailyImageBase64 != null && !dailyImageBase64.isEmpty()) {
                // data URL 접두사가 있으면 제거
                dailyImageBase64 = dailyImageBase64.replaceAll("^data:image/\\w+;base64,", "");
                byte[] dailyImageBytes = Base64.getDecoder().decode(dailyImageBase64);
                // 새 byte[] 복사본 생성
                byte[] dailyImageBytesCopy = java.util.Arrays.copyOf(dailyImageBytes, dailyImageBytes.length);
                ImageData dailyImageData = ImageDataFactory.create(dailyImageBytesCopy);
                Image dailyImage = new Image(dailyImageData);
                dailyImage.scaleToFit(IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT);
                document.add(new Paragraph("일별 매출 그래프").setFontSize(14).setBold());
                document.add(dailyImage);
                document.add(new Paragraph("\n"));
            }
            if (hourlyImageBase64 != null && !hourlyImageBase64.isEmpty()) {
                hourlyImageBase64 = hourlyImageBase64.replaceAll("^data:image/\\w+;base64,", "");
                byte[] hourlyImageBytes = Base64.getDecoder().decode(hourlyImageBase64);
                byte[] hourlyImageBytesCopy = java.util.Arrays.copyOf(hourlyImageBytes, hourlyImageBytes.length);
                ImageData hourlyImageData = ImageDataFactory.create(hourlyImageBytesCopy);
                Image hourlyImage = new Image(hourlyImageData);
                hourlyImage.scaleToFit(IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT);
                document.add(new Paragraph("시간별 매출 그래프").setFontSize(14).setBold());
                document.add(hourlyImage);
                document.add(new Paragraph("\n"));
            }
        }
    }

    private void addDetailedTables(Document document, DashboardMetrics metrics, PdfFont font) {
        Paragraph prodTitle = new Paragraph("인기 상품 TOP 10")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setBold()
            .setMarginTop(30)
            .setMarginBottom(10);
        document.add(prodTitle);
        Table productsTable = createProductsTable(metrics.getTop10Products(), font);
        document.add(productsTable);

        Paragraph performersTitle = new Paragraph("TOP 5 판매자 & 구매자")
            .setFontSize(16)
            .setFontColor(PRIMARY_COLOR)
            .setBold()
            .setMarginTop(30)
            .setMarginBottom(10);
        document.add(performersTitle);
        Table performersTable = createPerformersTable(metrics.getTop5Sellers(), metrics.getTop5GoodConsumers(), font);
        document.add(performersTable);
    }

    private void addTableHeader(Table table, String[] headers, PdfFont font) {
        for (String header : headers) {
            Cell cell = new Cell().add(new Paragraph(header).setFont(font))
                .setFontColor(ColorConstants.GRAY)
                .setBackgroundColor(new DeviceRgb(245, 245, 245))
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
            table.addCell(cell);
        }
    }

    private Table createProductsTable(List<ProductMetricsResponse> products, PdfFont font) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 15, 25, 10, 15, 10, 10, 10}))
            .useAllAvailableWidth();
        addTableHeader(table, new String[]{"순위", "카테고리", "상품명", "셀러", "가격", "판매량", "평점", "리뷰수"}, font);
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
        table.setFontSize(9)
            .setTextAlignment(TextAlignment.CENTER)
            .setMarginTop(10)
            .setMarginBottom(20);
        return table;
    }

    private Table createPerformersTable(List<MemberMetricsResponse> sellers, List<MemberMetricsResponse> consumers, PdfFont font) {
        Table table = new Table(UnitValue.createPercentArray(new float[]{5, 25, 20, 5, 25, 20}))
            .useAllAvailableWidth();

        Cell sellerHeader = new Cell(1, 3)
            .add(new Paragraph("판매자 TOP 5").setFont(font))
            .setFontColor(ColorConstants.WHITE)
            .setBackgroundColor(PRIMARY_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER);
        Cell consumerHeader = new Cell(1, 3)
            .add(new Paragraph("구매자 TOP 5").setFont(font))
            .setFontColor(ColorConstants.WHITE)
            .setBackgroundColor(PRIMARY_COLOR)
            .setBold()
            .setTextAlignment(TextAlignment.CENTER);
        table.addHeaderCell(sellerHeader);
        table.addHeaderCell(consumerHeader);
        addTableHeader(table, new String[]{"순위", "닉네임", "판매실적", "순위", "닉네임", "구매실적"}, font);

        for (int i = 0; i < 5; i++) {
            if (i < sellers.size()) {
                MemberMetricsResponse seller = sellers.get(i);
                table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(seller.getNickName())).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(String.format("₩%,d", seller.getMonthlySales()))).setTextAlignment(TextAlignment.RIGHT));
            } else {
                table.addCell(new Cell().add(new Paragraph("-")).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-")).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-")).setTextAlignment(TextAlignment.CENTER));
            }
            if (i < consumers.size()) {
                MemberMetricsResponse consumer = consumers.get(i);
                table.addCell(new Cell().add(new Paragraph(String.valueOf(i + 1))).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph(consumer.getNickName())).setTextAlignment(TextAlignment.CENTER));
                String performance = String.format("₩%,d\n(%d개 리뷰)",
                    consumer.getMonthlyPurchase(),
                    consumer.getReviewCount());
                table.addCell(new Cell().add(new Paragraph(performance)).setTextAlignment(TextAlignment.RIGHT));
            } else {
                table.addCell(new Cell().add(new Paragraph("-")).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-")).setTextAlignment(TextAlignment.CENTER));
                table.addCell(new Cell().add(new Paragraph("-")).setTextAlignment(TextAlignment.CENTER));
            }
        }
        table.setFontSize(10)
            .setMarginTop(10)
            .setMarginBottom(20);
        return table;
    }
}
