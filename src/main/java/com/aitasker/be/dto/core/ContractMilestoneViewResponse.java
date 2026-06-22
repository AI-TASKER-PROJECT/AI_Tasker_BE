package com.aitasker.be.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BigDecimal getDifference() {
        if (originalBudget == null || finalBudget == null) return null;
        return finalBudget.subtract(originalBudget);
    }
}
