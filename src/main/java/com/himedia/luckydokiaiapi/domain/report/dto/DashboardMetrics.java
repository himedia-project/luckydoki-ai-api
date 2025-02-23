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

    private int newMemberCount;
    private int totalOrderCount;
    private long todayRevenue;
    private int totalCommunityCount;
    private List<ProductMetricsResponse> top10Products;
    private List<MemberMetricsResponse> top5Sellers;
    private List<MemberMetricsResponse> top5GoodConsumers;
}
