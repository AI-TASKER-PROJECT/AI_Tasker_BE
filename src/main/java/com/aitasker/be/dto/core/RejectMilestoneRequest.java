package com.aitasker.be.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectMilestoneRequest {
    private String reason;
    private List<FailedCriterionFeedback> failedCriteria;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedCriterionFeedback {
        private Integer criteriaId;
        private String reason;
    }
}
