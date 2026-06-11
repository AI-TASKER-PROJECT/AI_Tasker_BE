package com.aitasker.be.dto.recommendation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RecommendExpertsRequest {
    @Min(1)
    @Max(50)
    private Integer maxCandidates;
}
