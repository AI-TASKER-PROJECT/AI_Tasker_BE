package com.aitasker.be.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardContractsResponse {
    private LocalDate from;
    private LocalDate to;
    private String groupBy;
    private Long totalContracts;
    private Long activeContracts;
    private Long completedContracts;
    private Long closedContracts;
    private Long terminatedContracts;
    private List<DashboardBreakdownItem> statusBreakdown;
    private List<DashboardTimeSeriesPoint> createdTrend;
}
