package com.aitasker.be.dto.core;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StaffDisputeListItem {
    private Integer disputeId;
    private Integer contractId;
    private Integer milestoneId;
    private Integer jobId;
    private String jobTitle;
    private String status;
    private String initiatedBy;
    private String initiationType;
    private String reason;
    private LocalDateTime createdAt;
    private List<String> jobDomains;
    private List<String> jobSkills;
    private List<String> matchedStaffDomains;
    private List<String> matchedStaffSkills;
    private LocalDateTime evidenceCollectionDueAt;
    private LocalDateTime staffSlaDueAt;
    private LocalDateTime staffReviewStartedAt;
    private LocalDateTime staffDecidedAt;
    private boolean staffDecisionMade;
}
