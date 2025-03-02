package com.himedia.luckydokiaiapi.domain.image.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageAnalysisDTO {

    private MultipartFile image;
    private String message = "이 이미지에 무엇이 있나요?";
}
