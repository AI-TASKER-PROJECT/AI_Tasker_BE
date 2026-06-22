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
public class ExpertRecommendationResponse {
    private Long expertId;
    private Long portfolioId;
    private Integer rankPosition;
    private Double matchScore;
    private List<String> matchedSkills;
    private List<String> matchedDomains;
    private String reason;
    private Boolean businessSelected;
}
