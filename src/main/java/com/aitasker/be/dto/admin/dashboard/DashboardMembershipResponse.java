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
public class DashboardMembershipResponse {
    private LocalDate from;
    private LocalDate to;
    private String groupBy;
    private Long totalPurchases;
    private BigDecimal totalRevenue;
    private List<DashboardBreakdownItem> packageBreakdown;
    private List<DashboardTimeSeriesPoint> purchaseTrend;
}
