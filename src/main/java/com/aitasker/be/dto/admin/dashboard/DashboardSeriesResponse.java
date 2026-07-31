package com.aitasker.be.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSeriesResponse {
    private LocalDate from;
    private LocalDate to;
    private String groupBy;
    private BigDecimal totalAmount;
    private Long totalCount;
    private List<DashboardTimeSeriesPoint> series;
    private List<DashboardBreakdownItem> breakdown;
}
