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
public class AiExpertRankingResponseDto {
    private List<ExpertRecommendationItemDto> recommendations;
    private String note;
}
