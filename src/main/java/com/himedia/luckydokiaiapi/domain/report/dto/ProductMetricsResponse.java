package com.himedia.luckydokiaiapi.domain.report.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductMetricsResponse {
    private Long id;
    private String name;
    private String categoryName;
    private long discountPrice;
    private int salesCount;
    private double reviewAverage;
    private int reviewCount;
    private int likesCount;
    private String shopName;

    public static ProductMetricsResponse from(ProductDTO.Response product) {
        return ProductMetricsResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .discountPrice(product.getDiscountPrice())
                .salesCount(product.getSalesCount())
                .reviewAverage(product.getReviewAverage())
                .reviewCount(product.getReviewCount())
                .likesCount(product.getLikesCount())
                .shopName(product.getShopName())
                .build();
    }
}