/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/MarketplaceController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.ProposalEntity;
import com.aitasker.be.service.core.MarketplaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này đặt prefix đường dẫn API cho controller hoặc method.
@RequestMapping("/api/v1")
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class MarketplaceController {
    private final MarketplaceService marketplaceService;

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/jobs")
    // Note: Hàm `createJob` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<JobEntity>> createJob(@RequestBody JobEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE JOB SUCCESS", marketplaceService.createJob(request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<Object>> listJobs() { return ResponseEntity.ok(ApiResponse.success("LIST JOBS SUCCESS", marketplaceService.listJobs())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/my")
    // Note: Hàm `listMyJobs` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listMyJobs() { return ResponseEntity.ok(ApiResponse.success("LIST MY JOBS SUCCESS", marketplaceService.listMyJobs())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ApiResponse<JobEntity>> jobDetail(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("GET JOB SUCCESS", marketplaceService.getJob(jobId))); }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/proposals")
    // Note: Hàm `submitProposal` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<ProposalEntity>> submitProposal(@RequestBody ProposalEntity request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT PROPOSAL SUCCESS", marketplaceService.submitProposal(request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/proposals/my")
    // Note: Hàm `listMyProposals` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listMyProposals() {
        return ResponseEntity.ok(ApiResponse.success("LIST MY PROPOSALS SUCCESS", marketplaceService.listMyProposals()));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}/proposals")
    // Note: Hàm `listProposals` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> listProposals(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST PROPOSALS SUCCESS", marketplaceService.listProposalsByJob(jobId)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/jobs/{jobId}/status")
    // Note: Hàm `updateJobStatus` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<JobEntity>> updateJobStatus(@PathVariable Integer jobId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE JOB STATUS SUCCESS", marketplaceService.updateJobStatus(jobId, status)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/proposals/{proposalId}/status")
    // Note: Hàm `reviewProposal` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<ProposalEntity>> reviewProposal(@PathVariable Integer proposalId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("REVIEW PROPOSAL SUCCESS", marketplaceService.reviewProposal(proposalId, status)));
    }
}
