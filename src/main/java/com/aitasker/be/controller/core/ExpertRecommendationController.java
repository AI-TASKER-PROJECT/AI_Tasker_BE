package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.candidate.ExpertRecommendationListResponse;
import com.aitasker.be.service.core.ExpertRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Expert Recommendations", description = "Generate and read saved AI expert recommendations for jobs")
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class ExpertRecommendationController {
    private final ExpertRecommendationService expertRecommendationService;

    @Operation(summary = "Generate expert recommendations", description = "Generate and persist Top 5 expert recommendations for a job.")
    @PostMapping("/{jobPostingId}/expert-recommendations")
    public ResponseEntity<ApiResponse<ExpertRecommendationListResponse>> generateRecommendations(
            @PathVariable Long jobPostingId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "GENERATE EXPERT RECOMMENDATIONS SUCCESS",
                expertRecommendationService.generateRecommendations(jobPostingId)
        ));
    }

    @Operation(summary = "Get saved expert recommendations", description = "Read saved expert recommendations without calling AI.")
    @GetMapping("/{jobPostingId}/expert-recommendations")
    public ResponseEntity<ApiResponse<ExpertRecommendationListResponse>> getRecommendations(
            @PathVariable Long jobPostingId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "GET EXPERT RECOMMENDATIONS SUCCESS",
                expertRecommendationService.getRecommendations(jobPostingId)
        ));
    }

    @Operation(summary = "Select a recommended expert", description = "Mark a saved recommendation as selected by the Business and notify the Expert to submit a proposal.")
    @PostMapping("/{jobPostingId}/expert-recommendations/{expertId}/select")
    public ResponseEntity<ApiResponse<Object>> selectRecommendedExpert(
            @PathVariable Long jobPostingId,
            @PathVariable Long expertId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "SELECT EXPERT RECOMMENDATION SUCCESS",
                expertRecommendationService.selectRecommendedExpert(jobPostingId, expertId)
        ));
    }
}
