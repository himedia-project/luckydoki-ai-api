package com.himedia.luckydokiaiapi.domain.report.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMetrics {
    // 최근 한달간 총 주문수
    private int totalOrderCount;
    // 최근 한달간 총 매출
    private int monthlyRevenue;
    // 오늘의 매출
    private long todayRevenue;
    // 한달 내 신규 회원수
    private int newMemberCount;
    // 한달 내 신규 셀러수
    private int newSellerCount;
    // 총 커뮤니티 수
    private int totalCommunityCount;
    // 인기 상품 Top 10 (기준: 리뷰평점(평균 평점 × 2) + 리뷰 수 + 좋아요 수 + 주문 수)
    private List<ProductMetricsResponse> top10Products;

    // top 5 sellers(좋아요 수 + 판매량)
    private List<MemberMetricsResponse> top5Sellers;
    // top 5 GoodConsumer(많이 구매하고 && review를 content를 10자 이상 쓴)
    private List<MemberMetricsResponse> top5GoodConsumers;
}
