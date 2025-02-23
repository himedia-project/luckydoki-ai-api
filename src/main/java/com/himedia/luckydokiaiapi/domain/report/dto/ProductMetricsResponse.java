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
    private String categoryAllName;
    private long discountPrice;
    private int salesCount;
    private double reviewAverage;
    private int reviewCount;
    private int likesCount;
    private String shopName;
}