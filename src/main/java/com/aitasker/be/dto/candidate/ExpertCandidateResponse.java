package com.aitasker.be.dto.candidate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertCandidateResponse {
    private Integer expertId;
    private Integer portfolioId;
    private Double matchScore;
    private List<String> matchedSkills;
    private List<String> matchedDomains;
    private Integer yearsExperience;
    private String certificates;
    private String selfDescription;
}
