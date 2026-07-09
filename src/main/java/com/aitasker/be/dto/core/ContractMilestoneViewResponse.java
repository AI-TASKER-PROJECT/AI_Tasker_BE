/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/core/ContractMilestoneViewResponse.java
 * Day la file gi: File DTO khai bao du lieu request/response di qua API hoac qua service.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation nay giup Lombok sinh getter, setter va cac ham tien ich cho du lieu.
@Data
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
// Note: Annotation nay giup Lombok sinh constructor rong cho JPA hoac deserialize du lieu.
@NoArgsConstructor
// Note: Annotation nay giup Lombok sinh constructor nhan day du field.
@AllArgsConstructor
public class ContractMilestoneViewResponse {
    private Integer contractMilestoneId;
    private Integer contractId;
    private Integer jobMilestoneId;
    private String milestoneName;
    private String description;
    private BigDecimal originalBudget;
    private BigDecimal finalBudget;
    private Integer orderIndex;
    private String status;
    private Integer duration;
    private String durationUnit;
    private String criteriaSnapshot;
    private String deliverableExpectation;
    private LocalDateTime dueAt;
    private Boolean overdue;
    private Integer progressReportRequestCount;
    private LocalDateTime progressReportRequestedAt;
    private LocalDateTime progressReportDueAt;
    private LocalDateTime progressReportSubmittedAt;
    private Boolean progressReportRequestPending;
    private Boolean progressReportRequestOverdue;
    private Integer rejectCount;
    private String lastRejectionFeedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Note: Ham `getDifference` phuc vu tao hoac doc du lieu truyen qua API.
    public BigDecimal getDifference() {
        if (originalBudget == null || finalBudget == null) return null;
        return finalBudget.subtract(originalBudget);
    }
}
