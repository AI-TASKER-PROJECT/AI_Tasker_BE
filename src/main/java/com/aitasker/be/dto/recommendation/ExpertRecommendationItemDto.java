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
public class ExpertRecommendationItemDto {
    private Integer expertId;
    private String fullName;
    private BigDecimal matchScore;
    private List<String> matchedSkills;
    private String reason;
    private String riskNotes;
    private String suggestedRole;
}
