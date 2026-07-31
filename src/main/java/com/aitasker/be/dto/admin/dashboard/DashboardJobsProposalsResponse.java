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
public class DashboardJobsProposalsResponse {
    private LocalDate from;
    private LocalDate to;
    private String groupBy;
    private Long totalJobs;
    private Long openJobs;
    private Long totalProposals;
    private Long acceptedProposals;
    private BigDecimal proposalAcceptanceRatePercent;
    private List<DashboardBreakdownItem> jobStatusBreakdown;
    private List<DashboardBreakdownItem> proposalStatusBreakdown;
    private List<DashboardTimeSeriesPoint> jobCreatedTrend;
    private List<DashboardTimeSeriesPoint> proposalCreatedTrend;
}
