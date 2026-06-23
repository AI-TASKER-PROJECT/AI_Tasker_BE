/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/ExpertCandidateController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
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

// Note: Annotation nay nhom API tren Swagger de nguoi doc de theo doi.
@Tag(name = "Expert Candidates", description = "Rank expert candidates for a job posting from saved SoW keywords")
// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/jobs")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class ExpertCandidateController {
    private final ExpertCandidateRankingService expertCandidateRankingService;

    // Note: Annotation nay mo ta endpoint tren Swagger de de test va doc tai lieu.
    @Operation(summary = "Find top expert candidates", description = "Extract SoW keywords and rank matching expert portfolios.")
    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/{jobPostingId}/expert-candidates")
    // Note: Ham `findTopCandidates` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ExpertCandidateSearchResponse findTopCandidates(@PathVariable Integer jobPostingId) {
        return expertCandidateRankingService.findTopCandidatesByJobPostingId(jobPostingId);
    }
}
