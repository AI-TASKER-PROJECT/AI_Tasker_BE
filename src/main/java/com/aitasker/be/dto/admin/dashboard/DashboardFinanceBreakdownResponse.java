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
public class DashboardFinanceBreakdownResponse {
    private LocalDate from;
    private LocalDate to;
    private BigDecimal systemCurrentBalance;
    private BigDecimal systemAvailableBalance;
    private BigDecimal systemEscrowBalance;
    private BigDecimal systemTotalRevenue;
    private BigDecimal pendingWithdrawalAmount;
    private BigDecimal approvedWithdrawalAmount;
    private BigDecimal grossTransactionVolume;
    private List<DashboardBreakdownItem> transactionTypeBreakdown;
    private List<DashboardBreakdownItem> withdrawalStatusBreakdown;
}
