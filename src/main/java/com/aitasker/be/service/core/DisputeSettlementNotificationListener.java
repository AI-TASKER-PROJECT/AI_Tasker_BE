package com.aitasker.be.service.core;

import com.aitasker.be.event.DisputeSettlementCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DisputeSettlementNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDisputeSettlementCompleted(DisputeSettlementCompletedEvent event) {
        for (Integer adminAccountId : event.adminAccountIds()) {
            try {
                notificationService.notifyAdminDisputeSettlementReported(
                        adminAccountId,
                        event.actorAccountId(),
                        event.disputeId(),
                        event.contractId(),
                        event.milestoneId(),
                        event.expertPayoutPercentage(),
                        event.expertPayoutAmount(),
                        event.businessRefundAmount(),
                        event.settlementWalletTransactionId());
            } catch (DataIntegrityViolationException ignored) {
                // A concurrent/replayed event lost the unique idempotency-key race.
            }
        }
    }
}
