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
public class DashboardUsersResponse {
    private LocalDate from;
    private LocalDate to;
    private String groupBy;
    private Long totalUsers;
    private Long pendingProfileReviews;
    private List<DashboardBreakdownItem> roleBreakdown;
    private List<DashboardBreakdownItem> statusBreakdown;
    private List<DashboardTimeSeriesPoint> newUsersTrend;
}
