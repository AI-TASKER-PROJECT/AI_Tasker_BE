package com.aitasker.be.dto.payment;

import com.aitasker.be.entity.UserQuotaEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class QuotaResponse {
    private Integer accountId;
    private Integer jobPostQuotaBalance;
    private Integer proposalQuotaBalance;
    private LocalDateTime badgeExpiredAt;
    private Boolean premiumRecommendationVisible;
    private Boolean premiumActive;

    public static QuotaResponse from(UserQuotaEntity quota, boolean premiumActive) {
        return QuotaResponse.builder()
                .accountId(quota.getAccountId())
                .jobPostQuotaBalance(quota.getJobPostQuotaBalance())
                .proposalQuotaBalance(quota.getProposalQuotaBalance())
                .badgeExpiredAt(quota.getBadgeExpiredAt())
                .premiumRecommendationVisible(quota.getPremiumRecommendationVisible())
                .premiumActive(premiumActive)
                .build();
    }
}
