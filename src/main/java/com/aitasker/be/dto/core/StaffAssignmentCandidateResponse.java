package com.aitasker.be.dto.core;

import lombok.Builder;
import lombok.Data;

import java.util.List;

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
    private List<String> matchedDomains;
    private List<String> matchedSkills;
}
