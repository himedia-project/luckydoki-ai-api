package com.himedia.luckydokiaiapi.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    private final ChatModel chatModel;

    @Value("classpath:/system.message")
    private Resource defaultSystemMessage;

    public String analyzeImage(MultipartFile file, String message) {
        // MIME 타입 결정
        String contentType = file.getContentType();
        if (!MimeTypeUtils.IMAGE_PNG_VALUE.equals(contentType) &&
                !MimeTypeUtils.IMAGE_JPEG_VALUE.equals(contentType)) {
            throw new IllegalArgumentException("지원되지 않는 이미지 형식입니다.");
        }

        try {
            // Media 객체 생성
            Media media = new Media(MimeType.valueOf(contentType), file.getResource());
            // 사용자 메시지 생성
            UserMessage userMessage = new UserMessage(message, media);
            // 시스템 메시지 생성
            UserMessage systemMessage = new UserMessage(defaultSystemMessage);
            // AI 모델 호출
            return chatModel.call(userMessage, systemMessage);
        } catch (Exception e) {
            throw new RuntimeException("이미지 분석에 실패했습니다." + e.getMessage());
        }
    }

}
