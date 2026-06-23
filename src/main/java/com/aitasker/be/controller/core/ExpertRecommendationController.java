/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/ExpertRecommendationController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
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

// Note: Annotation nay nhom API tren Swagger de nguoi doc de theo doi.
@Tag(name = "Expert Recommendations", description = "Generate and read saved AI expert recommendations for jobs")
// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/jobs")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class ExpertRecommendationController {
    private final ExpertRecommendationService expertRecommendationService;

    // Note: Annotation nay mo ta endpoint tren Swagger de de test va doc tai lieu.
    @Operation(summary = "Generate expert recommendations", description = "Generate and persist Top 5 expert recommendations for a job.")
    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/{jobPostingId}/expert-recommendations")
    // Note: Ham `generateRecommendations` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<ExpertRecommendationListResponse>> generateRecommendations(
            // Note: Annotation nay doc bien tren URL path vao tham so ham.
            @PathVariable Long jobPostingId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "GENERATE EXPERT RECOMMENDATIONS SUCCESS",
                expertRecommendationService.generateRecommendations(jobPostingId)
        ));
    }

    // Note: Annotation nay mo ta endpoint tren Swagger de de test va doc tai lieu.
    @Operation(summary = "Get saved expert recommendations", description = "Read saved expert recommendations without calling AI.")
    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/{jobPostingId}/expert-recommendations")
    // Note: Ham `getRecommendations` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<ExpertRecommendationListResponse>> getRecommendations(
            // Note: Annotation nay doc bien tren URL path vao tham so ham.
            @PathVariable Long jobPostingId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "GET EXPERT RECOMMENDATIONS SUCCESS",
                expertRecommendationService.getRecommendations(jobPostingId)
        ));
    }

    // Note: Annotation nay mo ta endpoint tren Swagger de de test va doc tai lieu.
    @Operation(summary = "Select a recommended expert", description = "Mark a saved recommendation as selected by the Business and notify the Expert to submit a proposal.")
    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/{jobPostingId}/expert-recommendations/{expertId}/select")
    // Note: Ham `selectRecommendedExpert` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<Object>> selectRecommendedExpert(
            // Note: Annotation nay doc bien tren URL path vao tham so ham.
            @PathVariable Long jobPostingId,
            // Note: Annotation nay doc bien tren URL path vao tham so ham.
            @PathVariable Long expertId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "SELECT EXPERT RECOMMENDATION SUCCESS",
                expertRecommendationService.selectRecommendedExpert(jobPostingId, expertId)
        ));
    }
}
