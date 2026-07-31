/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/MarketplaceController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.core.ProposalRequest;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.ProposalEntity;
import com.aitasker.be.service.core.MarketplaceService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    // Chức năng 1: Nhận request tạo Job nháp từ doanh nghiệp.
    public ResponseEntity<ApiResponse<JobEntity>> createJob(@RequestBody JobEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE JOB SUCCESS", marketplaceService.createJob(request)));
    }

    @PutMapping("/jobs/{jobId}")
    // Chức năng 2: Nhận request cập nhật Job nháp trước khi đăng bài.
    public ResponseEntity<ApiResponse<JobEntity>> updateDraftJob(@PathVariable Integer jobId, @RequestBody JobEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE DRAFT JOB SUCCESS", marketplaceService.updateDraftJob(jobId, request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs")
    @SecurityRequirements
    // Note: Hàm `listJobs` trả về các job đang OPEN để chuyên gia nhìn thấy trên marketplace.
    // Chức năng 3: Trả danh sách Job cho marketplace.
    public ResponseEntity<ApiResponse<Object>> listJobs() { return ResponseEntity.ok(ApiResponse.success("LIST JOBS SUCCESS", marketplaceService.listJobs())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/my")
    // Note: Hàm `listMyJobs` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    // Chức năng 4: Trả danh sách Job của doanh nghiệp hiện tại.
    public ResponseEntity<ApiResponse<Object>> listMyJobs() { return ResponseEntity.ok(ApiResponse.success("LIST MY JOBS SUCCESS", marketplaceService.listMyJobs())); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}")
    @SecurityRequirements
    // Note: Hàm `jobDetail` lấy chi tiết job, cho public xem job OPEN và chỉ chủ doanh nghiệp xem job nháp của mình.
    // Chức năng 5: Trả chi tiết Job theo jobId.
    public ResponseEntity<ApiResponse<JobEntity>> jobDetail(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("GET JOB SUCCESS", marketplaceService.getJob(jobId))); }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/proposals")
    // Note: Hàm `submitProposal` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    // Chức năng 6: Nhận request nộp proposal từ chuyên gia.
    public ResponseEntity<ApiResponse<ProposalEntity>> submitProposal(@RequestBody ProposalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT PROPOSAL SUCCESS", marketplaceService.submitProposal(request)));
    }

    @PutMapping("/proposals/{proposalId}")
    public ResponseEntity<ApiResponse<ProposalEntity>> updateProposal(@PathVariable Integer proposalId, @RequestBody ProposalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE PROPOSAL SUCCESS", marketplaceService.updateProposal(proposalId, request)));
    }

    // Note: Annotation này khai báo API upload file bằng multipart/form-data.
    @PostMapping(value = "/proposals/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    // Note: Hàm `uploadProposalFile` nhận file proposal của chuyên gia, upload Firebase và trả path để gửi kèm proposal.
    // Chức năng 7: Nhận file đính kèm proposal và trả về đường dẫn lưu trữ.
    public ResponseEntity<ApiResponse<String>> uploadProposalFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success("UPLOAD PROPOSAL FILE SUCCESS", marketplaceService.uploadProposalFile(file)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/proposals/my")
    // Note: Hàm `listMyProposals` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    // Chức năng 8: Trả danh sách proposal của chuyên gia hiện tại.
    public ResponseEntity<ApiResponse<Object>> listMyProposals() {
        return ResponseEntity.ok(ApiResponse.success("LIST MY PROPOSALS SUCCESS", marketplaceService.listMyProposals()));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}/proposals")
    // Note: Hàm `listProposals` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    // Chức năng 9: Trả danh sách proposal của một Job cho doanh nghiệp.
    public ResponseEntity<ApiResponse<Object>> listProposals(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("LIST PROPOSALS SUCCESS", marketplaceService.listProposalsByJob(jobId)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/jobs/{jobId}/status")
    // Note: Hàm `updateJobStatus` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    // Chức năng 10: Nhận request đổi trạng thái Job.
    public ResponseEntity<ApiResponse<JobEntity>> updateJobStatus(@PathVariable Integer jobId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE JOB STATUS SUCCESS", marketplaceService.updateJobStatus(jobId, status)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PostMapping("/jobs/{jobId}/publish")
    // Chức năng 11: Publish Job nháp sang trạng thái OPEN.
    public ResponseEntity<ApiResponse<JobEntity>> publishJob(@PathVariable Integer jobId) {
        return ResponseEntity.ok(ApiResponse.success("PUBLISH JOB SUCCESS", marketplaceService.updateJobStatus(jobId, "OPEN")));
    }

    @PatchMapping("/proposals/{proposalId}/status")
    // Note: Hàm `reviewProposal` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    // Chức năng 12: Nhận request chấp nhận hoặc từ chối proposal.
    public ResponseEntity<ApiResponse<ProposalEntity>> reviewProposal(@PathVariable Integer proposalId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("REVIEW PROPOSAL SUCCESS", marketplaceService.reviewProposal(proposalId, status)));
    }
}
