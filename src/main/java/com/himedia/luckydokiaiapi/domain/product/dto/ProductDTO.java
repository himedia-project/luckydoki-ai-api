package com.himedia.luckydokiaiapi.domain.product.dto;

import lombok.*;

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
    public static class Response {
        private Long id;
        private String code;
        private Long categoryId;
        private String categoryAllName;
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
        private String isNew;
        private String best;
        private String event;
        private Long shopId;
        private String shopName;
        private String shopImage;
        private Integer stockNumber;
        private String nickName;
        private String email;

        @Builder.Default
        private List<String> uploadFileNames = new ArrayList<>();
        private List<String> tagStrList;
        private String createdAt;
        private String modifiedAt;
        private Boolean likes;

    }
}
