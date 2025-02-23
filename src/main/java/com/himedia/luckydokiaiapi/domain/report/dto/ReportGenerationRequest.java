package com.himedia.luckydokiaiapi.domain.report.dto;

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

}
