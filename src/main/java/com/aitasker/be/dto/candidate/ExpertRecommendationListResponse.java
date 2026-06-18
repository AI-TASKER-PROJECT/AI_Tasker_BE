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
public class ExpertRecommendationListResponse {
    private Long jobPostingId;
    private List<ExpertRecommendationResponse> recommendations;
    private Boolean generatedByAi;
    private String message;
}
