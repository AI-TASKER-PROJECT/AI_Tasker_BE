package com.aitasker.be.dto.recommendation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpertCandidateDto {
    private Integer expertId;
    private String fullName;
    private String profile;
    private List<String> skills;
    private List<String> domains;
    private Integer yearsExperience;
    private BigDecimal rating;
    private Integer completedProjects;
    private BigDecimal hourlyRate;
    private String availability;
    private String portfolioUrl;
}
