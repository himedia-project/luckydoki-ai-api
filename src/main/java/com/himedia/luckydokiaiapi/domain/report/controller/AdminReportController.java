package com.himedia.luckydokiaiapi.domain.report.controller;

import com.himedia.luckydokiaiapi.domain.report.dto.ReportGenerationRequest;
import com.himedia.luckydokiaiapi.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequestMapping("/api/admin/report")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    @PostMapping("/generate")
    public ResponseEntity<Resource> generateReport(@RequestBody ReportGenerationRequest request) {
        log.info("generateReport request: {}", request);
        try {
            // 리포트 생성
            String pdfPath = reportService.generateMonthlyReport(request);

            // 생성된 PDF 파일을 Resource로 변환
            Resource resource = new FileSystemResource(new File(pdfPath));

            // 파일명 설정
            String fileName = String.format("monthly-report-%s.pdf",
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM")));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("리포트 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

