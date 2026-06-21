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
    private LocalDateTime premiumExpiredAt;
    private Boolean premiumActive;
    private String activePackageCode;
    private String activePackageName;

    public static QuotaResponse from(
            UserQuotaEntity quota,
            boolean premiumActive,
            String activePackageCode,
            String activePackageName
    ) {
        return QuotaResponse.builder()
                .accountId(quota.getAccountId())
                .jobPostQuotaBalance(quota.getJobPostQuotaBalance())
                .proposalQuotaBalance(quota.getProposalQuotaBalance())
                .badgeExpiredAt(quota.getBadgeExpiredAt())
                .premiumExpiredAt(quota.getPremiumExpiredAt())
                .premiumActive(premiumActive)
                .activePackageCode(activePackageCode)
                .activePackageName(activePackageName)
                .build();
    }
}
