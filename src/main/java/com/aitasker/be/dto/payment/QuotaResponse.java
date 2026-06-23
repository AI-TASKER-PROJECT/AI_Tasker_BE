/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/payment/QuotaResponse.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.payment;

import com.aitasker.be.entity.UserQuotaEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
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

    // Note: Ham `from` phuc vu tao hoac doc du lieu truyen qua API.
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
