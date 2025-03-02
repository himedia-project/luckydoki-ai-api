package com.himedia.luckydokiaiapi.domain.image.controller;

import com.himedia.luckydokiaiapi.domain.image.dto.ImageAnalysisDTO;
import com.himedia.luckydokiaiapi.domain.image.service.ImageAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@RequestMapping("/api/ai/image")
@RestController
@RequiredArgsConstructor
public class ImageController {

    @Value("${upload.path}")
    private String uploadPath;

    private final ImageAnalysisService imageAnalysisService;

    // 이미지 분석
    @PostMapping("/analyze")
    public ResponseEntity<List<String>> analyzeImage(
            ImageAnalysisDTO imageAnalysisDTO
    ) throws IOException {
        File uploadDirectory = new File(uploadPath);
        if(!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        MultipartFile file = imageAnalysisDTO.getImage();
        String message = imageAnalysisDTO.getMessage();

        log.info("Image received: {}", file.getOriginalFilename());
        log.info("Message received: {}", message);

        String filename = file.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, filename);
        Files.write(filePath, file.getBytes()); // 업로드

        String analysisText = imageAnalysisService.analyzeImage(file, message);
        // ","로 분리된 결과를 List<String>으로 변환
        List<String> analysisResults = List.of(analysisText.split(","));

        return ResponseEntity.ok(analysisResults);
    }

}
