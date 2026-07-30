package com.aitasker.be.service.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "app.sla.auto-approval.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class MilestoneReviewSlaScheduler {
    private final ContractExecutionService contractExecutionService;

    @Scheduled(
            fixedDelayString = "${app.sla.auto-approval.fixed-delay-ms:60000}",
            initialDelayString = "${app.sla.auto-approval.initial-delay-ms:10000}"
    )
    public void processDueMilestoneReviews() {
        try {
            int processed = contractExecutionService.processDueReviewSla().size();
            if (processed > 0) {
                log.info("Automatically approved {} milestone review(s) after SLA expiry", processed);
            }
        } catch (Exception exception) {
            log.error("Automatic milestone review SLA cycle failed", exception);
        }
    }
}
