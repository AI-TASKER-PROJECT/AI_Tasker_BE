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
public class ExpertCandidateSearchResponse {
    private Integer jobPostingId;
    private SowKeywordExtractionResult keywords;
    private List<ExpertCandidateResponse> candidates;
}
