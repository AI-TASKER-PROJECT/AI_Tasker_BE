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
public class DashboardDisputesResponse {
    private LocalDate from;
    private LocalDate to;
    private String groupBy;
    private Long totalDisputes;
    private Long openDisputes;
    private Long resolvedDisputes;
    private Long overdueStaffSlaDisputes;
    private List<DashboardBreakdownItem> statusBreakdown;
    private List<DashboardTimeSeriesPoint> createdTrend;
}
