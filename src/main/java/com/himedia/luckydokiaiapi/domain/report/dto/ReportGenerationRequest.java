package com.himedia.luckydokiaiapi.domain.report.dto;

import com.himedia.luckydokiaiapi.domain.sales.dto.SalesData;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationRequest {

    private LocalDate startDate;
    private LocalDate endDate;
    private DashboardMetrics metrics;
    private List<SalesData> dailySalesData;
    private List<SalesData> hourlySalesData;

}
