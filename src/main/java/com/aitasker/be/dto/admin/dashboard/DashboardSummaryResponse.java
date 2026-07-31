package com.aitasker.be.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {
    private Long totalUsers;
    private Long businessUsers;
    private Long expertUsers;
    private Long staffUsers;
    private Long pendingProfileReviews;
    private Long totalJobs;
    private Long openJobs;
    private Long totalProposals;
    private Long acceptedProposals;
    private Long totalContracts;
    private Long activeContracts;
    private Long completedContracts;
    private Long closedContracts;
    private Long terminatedContracts;
    private Long totalDisputes;
    private Long openDisputes;
    private Long totalMembershipPurchases;
    private BigDecimal totalMembershipRevenue;
    private BigDecimal grossTransactionVolume;
    private BigDecimal systemAvailableBalance;
    private BigDecimal systemEscrowBalance;
    private BigDecimal pendingWithdrawalAmount;
}
