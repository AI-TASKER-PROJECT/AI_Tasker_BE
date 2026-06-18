package com.aitasker.be.controller.core;

import com.aitasker.be.dto.candidate.ExpertCandidateSearchResponse;
import com.aitasker.be.service.core.ExpertCandidateRankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Expert Candidates", description = "Rank expert candidates for a job posting from saved SoW keywords")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class ExpertCandidateController {
    private final ExpertCandidateRankingService expertCandidateRankingService;

    @Operation(summary = "Find top expert candidates", description = "Extract SoW keywords and rank matching expert portfolios.")
    @GetMapping("/{jobPostingId}/expert-candidates")
    public ExpertCandidateSearchResponse findTopCandidates(@PathVariable Integer jobPostingId) {
        return expertCandidateRankingService.findTopCandidatesByJobPostingId(jobPostingId);
    }
}
