/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/ContractExecutionController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.core.AcceptanceCriteriaRequest;
import com.aitasker.be.dto.core.ContractMilestoneViewResponse;
import com.aitasker.be.dto.core.ProgressReportRequest;
import com.aitasker.be.dto.payment.DepositRefundRequest;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.service.core.AdminService;
import com.aitasker.be.service.core.ContractExecutionService;
import com.aitasker.be.service.core.PaymentWalletService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này đặt prefix đường dẫn API cho controller hoặc method.
@RequestMapping("/api/v1")
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class ContractExecutionController {
    private final ContractExecutionService service;
    private final PaymentWalletService paymentWalletService;
    private final AdminService adminService;

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/contracts/from-proposals/{proposalId}")
    // Note: Hàm `createDraft` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<ContractEntity>> createDraft(@PathVariable Integer proposalId, @RequestBody ContractEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE CONTRACT DRAFT SUCCESS", service.createDraftFromProposal(proposalId, request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/contracts/{contractId}/sign")
    // Note: Hàm `signContract` xử lý API ký xác nhận hợp đồng của business hoặc expert.
    public ResponseEntity<ApiResponse<ContractEntity>> signContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("SIGN CONTRACT SUCCESS", service.signContract(contractId)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/contracts/{contractId}/nda-sign")
    // Note: Hàm `signNda` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<ContractEntity>> signNda(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("SIGN NDA SUCCESS", service.signNda(contractId)));
    }

    @PostMapping("/contracts/{contractId}/reject")
    public ResponseEntity<ApiResponse<ContractEntity>> rejectContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("REJECT CONTRACT SUCCESS", service.rejectContract(contractId)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/contracts/{contractId}/deposit/pay")
    public ResponseEntity<ApiResponse<PaymentActionResponse<ContractDepositEntity>>> payContractDeposit(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("PAY CONTRACT DEPOSIT SUCCESS",
                paymentWalletService.payContractDeposit(contractId)));
    }

    @PostMapping("/admin/contracts/{contractId}/deposit/refund")
    public ResponseEntity<ApiResponse<ContractDepositEntity>> refundContractDeposit(
            @PathVariable Integer contractId,
            @RequestBody DepositRefundRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REFUND CONTRACT DEPOSIT SUCCESS",
                paymentWalletService.refundContractDeposit(contractId, request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/milestones")
    // Note: Hàm `createMilestone` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<MilestoneEntity>> createMilestone(@RequestBody MilestoneEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE MILESTONE SUCCESS", service.createMilestone(request)));
    }

    @PostMapping("/milestones/{milestoneId}/criteria")
    public ResponseEntity<ApiResponse<AcceptanceCriteriaEntity>> createCriteria(
            @PathVariable Integer milestoneId,
            @RequestBody AcceptanceCriteriaRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "CREATE ACCEPTANCE CRITERIA SUCCESS",
                service.createCriteria(milestoneId, request)
        ));
    }

    @PutMapping("/milestones/{milestoneId}/criteria/{criteriaId}")
    public ResponseEntity<ApiResponse<AcceptanceCriteriaEntity>> updateCriteria(
            @PathVariable Integer milestoneId,
            @PathVariable Integer criteriaId,
            @RequestBody AcceptanceCriteriaRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "UPDATE ACCEPTANCE CRITERIA SUCCESS",
                service.updateCriteria(milestoneId, criteriaId, request)
        ));
    }

    @DeleteMapping("/milestones/{milestoneId}/criteria/{criteriaId}")
    public ResponseEntity<ApiResponse<Void>> deleteCriteria(
            @PathVariable Integer milestoneId,
            @PathVariable Integer criteriaId
    ) {
        service.deleteCriteria(milestoneId, criteriaId);
        return ResponseEntity.ok(ApiResponse.success("DELETE ACCEPTANCE CRITERIA SUCCESS", null));
    }

    @PostMapping("/milestones/{milestoneId}/complete")
    public ResponseEntity<ApiResponse<MilestoneEntity>> completeMilestone(@PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("COMPLETE MILESTONE SUCCESS", service.completeMilestone(milestoneId)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/deposit")
    public ResponseEntity<ApiResponse<MilestoneEntity>> depositMilestone(@PathVariable Integer contractId, @PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("DEPOSIT MILESTONE SUCCESS", service.depositMilestoneEscrow(contractId, milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/start")
    public ResponseEntity<ApiResponse<MilestoneEntity>> startMilestone(@PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("START MILESTONE SUCCESS", service.startMilestone(milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/deliverables")
    public ResponseEntity<ApiResponse<DeliverableEntity>> submitMilestoneDeliverable(@PathVariable Integer milestoneId, @RequestBody DeliverableEntity request) {
        request.setMilestoneId(milestoneId);
        return ResponseEntity.ok(ApiResponse.success("SUBMIT DELIVERABLE SUCCESS", service.submitDeliverable(request)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/progress-reports")
    public ResponseEntity<ApiResponse<MilestoneProgressReportEntity>> submitProgressReport(@PathVariable Integer contractId, @PathVariable Integer milestoneId, @RequestBody ProgressReportRequest request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT PROGRESS REPORT SUCCESS", service.submitProgressReport(contractId, milestoneId, request.getContent(), request.getPercentComplete(), request.getAttachmentUrl())));
    }

    @GetMapping("/contracts/{contractId}/milestones/{milestoneId}/progress-reports")
    public ResponseEntity<ApiResponse<List<MilestoneProgressReportEntity>>> listProgressReports(@PathVariable Integer contractId, @PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("LIST PROGRESS REPORT SUCCESS", service.listProgressReports(contractId, milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/approve")
    public ResponseEntity<ApiResponse<MilestoneEntity>> approveMilestone(@PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE MILESTONE SUCCESS", service.approveMilestone(milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/reject")
    public ResponseEntity<ApiResponse<MilestoneEntity>> rejectMilestone(@PathVariable Integer milestoneId, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("REJECT MILESTONE SUCCESS", service.rejectMilestone(milestoneId, reason)));
    }

    @PatchMapping("/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneEntity>> updateMilestone(@PathVariable Integer milestoneId, @RequestBody MilestoneEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE MILESTONE SUCCESS", service.updateMilestone(milestoneId, request)));
    }

    @PostMapping("/milestones/{milestoneId}/disputes")
    public ResponseEntity<ApiResponse<DisputeEntity>> initiateMilestoneDispute(@PathVariable Integer milestoneId, @RequestParam Integer contractId, @RequestParam(required = false) String initiatedBy, @RequestParam(required = false) String initiationType) {
        return ResponseEntity.ok(ApiResponse.success("INITIATE DISPUTE SUCCESS", service.initiateDispute(contractId, milestoneId, initiatedBy, initiationType)));
    }

    @PostMapping("/disputes/{disputeId}/escalation-request")
    public ResponseEntity<ApiResponse<DisputeEntity>> requestEscalation(@PathVariable Integer disputeId, @RequestParam(required = false) String reason, @RequestParam(required = false) String evidenceFile) {
        return ResponseEntity.ok(ApiResponse.success("ESCALATION REQUEST SUCCESS", service.escalateDispute(disputeId, reason, evidenceFile)));
    }

    @PostMapping("/disputes/{disputeId}/assign-staff")
    public ResponseEntity<ApiResponse<DisputeEntity>> assignDisputeStaff(@PathVariable Integer disputeId, @RequestParam Integer staffId) {
        return ResponseEntity.ok(ApiResponse.success("ASSIGN DISPUTE STAFF SUCCESS", service.assignDispute(disputeId, staffId)));
    }

    @PostMapping("/disputes/{disputeId}/reject-intervention")
    public ResponseEntity<ApiResponse<DisputeEntity>> rejectInterventionAlias(@PathVariable Integer disputeId, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("REJECT INTERVENTION SUCCESS", service.rejectIntervention(disputeId, reason)));
    }

    @PostMapping("/disputes/{disputeId}/staff-decision")
    public ResponseEntity<ApiResponse<DisputeEntity>> staffDecideAlias(@PathVariable Integer disputeId, @RequestParam Integer expertPercent, @RequestParam(required = false) String note, @RequestParam(required = false) String staffReport) {
        return ResponseEntity.ok(ApiResponse.success("STAFF DECISION SUCCESS", service.staffDecide(disputeId, expertPercent, note, staffReport)));
    }

    @PostMapping("/disputes/{disputeId}/execute-settlement")
    public ResponseEntity<ApiResponse<DisputeEntity>> executeDisputeSettlementAlias(@PathVariable Integer disputeId) {
        return ResponseEntity.ok(ApiResponse.success("EXECUTE DISPUTE SETTLEMENT SUCCESS", service.executeDisputeSettlement(disputeId)));
    }

    @PostMapping("/disputes/{disputeId}/cancel")
    public ResponseEntity<ApiResponse<DisputeEntity>> cancelDispute(@PathVariable Integer disputeId, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("CANCEL DISPUTE SUCCESS", service.cancelDispute(disputeId, reason)));
    }

    @PostMapping("/contracts/{contractId}/termination-requests")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> requestTermination(@PathVariable Integer contractId, @RequestBody TerminationRequestEntity request) {
        return ResponseEntity.ok(ApiResponse.success("REQUEST TERMINATION SUCCESS", service.requestTerminationRequest(contractId, request)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/assign-staff")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> assignTerminationStaff(@PathVariable Long terminationRequestId, @RequestParam Integer staffId) {
        return ResponseEntity.ok(ApiResponse.success("ASSIGN TERMINATION STAFF SUCCESS", service.assignTerminationStaff(terminationRequestId, staffId)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/reject")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> rejectTermination(@PathVariable Long terminationRequestId, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("REJECT TERMINATION SUCCESS", service.rejectTermination(terminationRequestId, reason)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/approve")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> approveTermination(@PathVariable Long terminationRequestId, @RequestBody(required = false) TerminationRequestEntity request) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE TERMINATION SUCCESS", service.approveTermination(terminationRequestId, request)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/partial-evidence")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> submitPartialEvidence(@PathVariable Long terminationRequestId, @RequestBody TerminationRequestEntity request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT PARTIAL EVIDENCE SUCCESS", service.submitPartialEvidence(terminationRequestId, request)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/execute-settlement")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> executeTerminationSettlement(@PathVariable Long terminationRequestId) {
        return ResponseEntity.ok(ApiResponse.success("EXECUTE TERMINATION SETTLEMENT SUCCESS", service.executeTerminationSettlement(terminationRequestId)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/withdraw")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> withdrawTermination(@PathVariable Long terminationRequestId, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("WITHDRAW TERMINATION SUCCESS", service.withdrawTerminationRequest(terminationRequestId, reason)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/refund-deposit")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> refundTerminationDeposit(@PathVariable Long terminationRequestId, @RequestBody DepositRefundRequest request) {
        return ResponseEntity.ok(ApiResponse.success("REFUND TERMINATION DEPOSIT SUCCESS", service.refundDepositAfterTermination(terminationRequestId, request)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<Object>> listContracts() { return ResponseEntity.ok(ApiResponse.success("LIST CONTRACTS SUCCESS", service.listContracts())); }

    @GetMapping("/contracts/{contractId}")
    public ResponseEntity<ApiResponse<Object>> getContract(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("GET CONTRACT SUCCESS", service.getContract(contractId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/contracts/{contractId}/milestones")
    public ResponseEntity<ApiResponse<List<ContractMilestoneViewResponse>>> listMilestones(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("LIST MILESTONES SUCCESS", service.listMilestonesByContract(contractId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}/milestones")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<Object>> listJobMilestones(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("LIST JOB MILESTONES SUCCESS", service.listMilestonesByJob(jobId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/milestones/{milestoneId}/criteria")
    public ResponseEntity<ApiResponse<Object>> listCriteria(@PathVariable Integer milestoneId) { return ResponseEntity.ok(ApiResponse.success("LIST CRITERIA SUCCESS", service.listCriteriaByMilestone(milestoneId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/milestones/{milestoneId}/deliverables")
    public ResponseEntity<ApiResponse<Object>> listDeliverables(@PathVariable Integer milestoneId) { return ResponseEntity.ok(ApiResponse.success("LIST DELIVERABLES SUCCESS", service.listDeliverablesByMilestone(milestoneId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/contracts/{contractId}/disputes")
    public ResponseEntity<ApiResponse<Object>> listDisputes(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("LIST DISPUTES SUCCESS", service.listDisputesByContract(contractId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/disputes/{disputeId}")
    public ResponseEntity<ApiResponse<DisputeEntity>> getDispute(@PathVariable Integer disputeId) { return ResponseEntity.ok(ApiResponse.success("GET DISPUTE SUCCESS", service.getDispute(disputeId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/contracts/{contractId}/termination-requests")
    public ResponseEntity<ApiResponse<List<TerminationRequestEntity>>> listTerminationRequests(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("LIST TERMINATION REQUESTS SUCCESS", service.listTerminationRequestsByContract(contractId)));
    }

    @GetMapping("/termination-requests/{terminationRequestId}")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> getTerminationRequest(@PathVariable Long terminationRequestId) {
        return ResponseEntity.ok(ApiResponse.success("GET TERMINATION REQUEST SUCCESS", service.getTerminationRequest(terminationRequestId)));
    }

    @PostMapping("/case-attachments")
    public ResponseEntity<ApiResponse<CaseAttachmentEntity>> createCaseAttachment(@RequestBody CaseAttachmentEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE CASE ATTACHMENT SUCCESS", service.createCaseAttachment(request)));
    }

    @GetMapping("/case-attachments")
    public ResponseEntity<ApiResponse<List<CaseAttachmentEntity>>> listCaseAttachments(@RequestParam String ownerType, @RequestParam Long ownerId) {
        return ResponseEntity.ok(ApiResponse.success("LIST CASE ATTACHMENTS SUCCESS", service.listCaseAttachments(ownerType, ownerId)));
    }

    @PostMapping("/contracts/{contractId}/reviews")
    public ResponseEntity<ApiResponse<ReviewEntity>> createContractReview(@PathVariable Integer contractId, @RequestBody ReviewEntity request) {
        request.setContractId(contractId);
        return ResponseEntity.ok(ApiResponse.success("CREATE REVIEW SUCCESS", adminService.createReview(request)));
    }

    @GetMapping("/contracts/{contractId}/reviews")
    public ResponseEntity<ApiResponse<Object>> listContractReviews(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("LIST REVIEWS SUCCESS", adminService.listReviewsByContract(contractId)));
    }

    @GetMapping("/jobs/{jobId}/matching")
    public ResponseEntity<ApiResponse<Object>> matching(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("MATCHING SUCCESS", service.matchingByKeyword(jobId))); }
}
