package com.himedia.luckydokiaiapi.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.himedia.luckydokiaiapi.domain.report.enums.ProductBest;
import com.himedia.luckydokiaiapi.domain.report.enums.ProductEvent;
import com.himedia.luckydokiaiapi.domain.report.enums.ProductIsNew;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class ProductDTO {


    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Builder
    @ToString
    public static class Response {
        private Long id;
        private String code;
        private Long categoryId;
        private String categoryName;
        private double reviewAverage;
        private int reviewCount;
        private int likesCount;
        // 판매량
        private int salesCount;
        private String name;
        private Integer price;
        private Integer discountPrice;
        private Integer discountRate;
        private String description;
        private ProductIsNew isNew;
        private ProductBest best;
        private ProductEvent event;
        private Long shopId;
        private String shopName;
        private String shopImage;
        private Integer stockNumber;
        private String nickName;
        private String email;

        @Builder.Default
        private List<String> uploadFileNames = new ArrayList<>();

        private List<String> tagStrList;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime modifiedAt;

        private Boolean likes;


    }
}
