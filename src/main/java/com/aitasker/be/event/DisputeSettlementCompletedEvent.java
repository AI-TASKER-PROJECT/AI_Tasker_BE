package com.aitasker.be.event;

import java.math.BigDecimal;
import java.util.List;

public record DisputeSettlementCompletedEvent(
        Integer actorAccountId,
        List<Integer> adminAccountIds,
        Integer disputeId,
        Integer contractId,
        Integer milestoneId,
        Integer expertPayoutPercentage,
        BigDecimal expertPayoutAmount,
        BigDecimal businessRefundAmount,
        Long settlementWalletTransactionId
) {
    public DisputeSettlementCompletedEvent {
        adminAccountIds = adminAccountIds == null ? List.of() : List.copyOf(adminAccountIds);
    }
}
