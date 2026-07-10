package com.aitasker.be.dto.core;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffAssignmentCandidateResponse {
    private Integer staffId;
    private String displayName;
    private String specializationMatch;
    private String technologyMatchSummary;
    private String availability;
    private Integer activeDisputeWorkloadCount;
    private Boolean conflictEligible;
}
