package com.aitasker.be.dto.core;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractChangeRequestRequest {
    private String changeType;
    private String changeSummary;
    private BigDecimal proposedBudget;
    private Integer proposedTimelineDays;
    private String proposedScope;
    @JsonAlias({"proposed_milestones", "milestones"})
    private List<ProposedContractMilestone> proposedMilestones;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProposedContractMilestone {
        private Integer contractMilestoneId;
        private Integer jobMilestoneId;
        @JsonAlias("name")
        private String milestoneName;
        private String description;
        @JsonAlias({"budget", "finalBudget"})
        private BigDecimal finalBudget;
        private Integer orderIndex;
        private Integer duration;
        private String durationUnit;
        private String criteriaSnapshot;
        private String deliverableExpectation;
    }
}
