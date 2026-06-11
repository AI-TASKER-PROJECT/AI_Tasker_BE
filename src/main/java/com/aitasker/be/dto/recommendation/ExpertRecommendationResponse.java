package com.aitasker.be.dto.recommendation;

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
    private Integer jobId;
    private String sowSummary;
    private SowKeywordsDto extractedKeywords;
    private Integer candidateCount;
    private String note;
    private List<ExpertRecommendationItemDto> recommendations;
}
