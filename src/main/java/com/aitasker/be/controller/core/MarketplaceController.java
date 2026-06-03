package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.ProposalEntity;
import com.aitasker.be.service.core.MarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MarketplaceController {
    private final MarketplaceService marketplaceService;

    @PostMapping("/jobs")
    public ResponseEntity<ApiResponse<JobEntity>> createJob(@RequestBody JobEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE JOB SUCCESS", marketplaceService.createJob(request)));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Object>> listJobs() { return ResponseEntity.ok(ApiResponse.success("LIST JOBS SUCCESS", marketplaceService.listJobs())); }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<JobEntity>> jobDetail(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("GET JOB SUCCESS", marketplaceService.getJob(jobId))); }

    @PostMapping("/proposals")
    public ResponseEntity<ApiResponse<ProposalEntity>> submitProposal(@RequestBody ProposalEntity request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT PROPOSAL SUCCESS", marketplaceService.submitProposal(request)));
    }

    @GetMapping("/jobs/{jobId}/proposals")
    public ResponseEntity<ApiResponse<Object>> listProposals(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST PROPOSALS SUCCESS", marketplaceService.listProposalsByJob(jobId)));
    }

    @PatchMapping("/jobs/{jobId}/status")
    public ResponseEntity<ApiResponse<JobEntity>> updateJobStatus(@PathVariable Integer jobId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE JOB STATUS SUCCESS", marketplaceService.updateJobStatus(jobId, status)));
    }

    @PatchMapping("/proposals/{proposalId}/status")
    public ResponseEntity<ApiResponse<ProposalEntity>> reviewProposal(@PathVariable Integer proposalId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("REVIEW PROPOSAL SUCCESS", marketplaceService.reviewProposal(proposalId, status)));
    }
}
