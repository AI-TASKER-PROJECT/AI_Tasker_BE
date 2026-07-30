/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/ContractExecutionController.java
 * ÄÃ¢y lÃ  file gÃ¬: File controller nháº­n request HTTP, gá»i service phÃ¹ há»£p vÃ  tráº£ response cho client.
 * Má»¥c Ä‘Ã­ch note: giáº£i thÃ­ch cÃ¡c annotation vÃ  hÃ m chÃ­nh Ä‘á»ƒ Ä‘á»c hiá»ƒu chá»©c nÄƒng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.core.AcceptanceCriteriaRequest;
import com.aitasker.be.dto.core.ContractChangeRequestRequest;
import com.aitasker.be.dto.core.ContractChangeReviewRequest;
import com.aitasker.be.dto.core.ContractMilestoneViewResponse;
import com.aitasker.be.dto.core.ProgressReportFeedbackRequest;
import com.aitasker.be.dto.core.ProgressReportRequest;
import com.aitasker.be.dto.core.RejectMilestoneRequest;
import com.aitasker.be.dto.core.ImmediateTerminationRequest;
import com.aitasker.be.dto.core.StaffAssignmentCandidateResponse;
import com.aitasker.be.dto.core.StaffDisputeFilter;
import com.aitasker.be.dto.core.StaffDisputeListResponse;
import com.aitasker.be.dto.payment.DepositRefundRequest;
import com.aitasker.be.dto.payment.ContractDepositRateResponse;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.service.core.AdminService;
import com.aitasker.be.service.core.ContractExecutionService;
import com.aitasker.be.service.core.PaymentWalletService;
import com.aitasker.be.service.core.StaffDisputeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Note: Annotation nÃ y biáº¿n class thÃ nh REST controller Ä‘á»ƒ nháº­n request vÃ  tráº£ JSON.
@RestController
// Note: Annotation nÃ y Ä‘áº·t prefix Ä‘Æ°á»ng dáº«n API cho controller hoáº·c method.
@RequestMapping("/api/v1")
// Note: Annotation nÃ y giÃºp Lombok sinh constructor cho cÃ¡c dependency final.
@RequiredArgsConstructor
public class ContractExecutionController {
    private final ContractExecutionService service;
    private final PaymentWalletService paymentWalletService;
    private final AdminService adminService;
    private final StaffDisputeService staffDisputeService;

    @GetMapping("/contracts/deposit-rates")
    @Operation(summary = "Get current contract deposit percentages")
    public ResponseEntity<ApiResponse<ContractDepositRateResponse>> getContractDepositRates() {
        return ResponseEntity.ok(ApiResponse.success("GET CONTRACT DEPOSIT RATES SUCCESS",
                paymentWalletService.getContractDepositRates()));
    }

    // Note: Annotation nÃ y khai bÃ¡o API táº¡o má»›i hoáº·c gá»­i dá»¯ liá»‡u báº±ng HTTP POST.
    @PostMapping("/contracts/from-proposals/{proposalId}")
    // Note: HÃ m `createDraft` xá»­ lÃ½ má»™t API endpoint, nháº­n request, gá»i service vÃ  tráº£ káº¿t quáº£ cho client.
    public ResponseEntity<ApiResponse<ContractEntity>> createDraft(@PathVariable Integer proposalId, @RequestBody ContractEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE CONTRACT DRAFT SUCCESS", service.createDraftFromProposal(proposalId, request)));
    }

    // Note: Annotation nÃ y khai bÃ¡o API táº¡o má»›i hoáº·c gá»­i dá»¯ liá»‡u báº±ng HTTP POST.
    @PostMapping("/contracts/{contractId}/sign")
    // Note: HÃ m `signContract` xá»­ lÃ½ API kÃ½ xÃ¡c nháº­n há»£p Ä‘á»“ng cá»§a business hoáº·c expert.
    public ResponseEntity<ApiResponse<ContractEntity>> signContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("SIGN CONTRACT SUCCESS", service.signContract(contractId)));
    }

    // Note: Annotation nÃ y khai bÃ¡o API táº¡o má»›i hoáº·c gá»­i dá»¯ liá»‡u báº±ng HTTP POST.
    @PostMapping("/contracts/{contractId}/nda-sign")
    // Note: HÃ m `signNda` xá»­ lÃ½ má»™t API endpoint, nháº­n request, gá»i service vÃ  tráº£ káº¿t quáº£ cho client.
    public ResponseEntity<ApiResponse<ContractEntity>> signNda(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("SIGN NDA SUCCESS", service.signNda(contractId)));
    }

    @PostMapping("/contracts/{contractId}/reject")
    public ResponseEntity<ApiResponse<ContractEntity>> rejectContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("REJECT CONTRACT SUCCESS", service.rejectContract(contractId)));
    }

    @PostMapping("/contracts/{contractId}/cancel-draft")
    public ResponseEntity<ApiResponse<ContractEntity>> cancelDraftContract(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("CANCEL CONTRACT DRAFT SUCCESS", service.cancelDraftContract(contractId)));
    }

    // Note: Annotation nÃ y khai bÃ¡o API táº¡o má»›i hoáº·c gá»­i dá»¯ liá»‡u báº±ng HTTP POST.
    @PostMapping("/contracts/{contractId}/deposit/pay")
    public ResponseEntity<ApiResponse<PaymentActionResponse<ContractDepositEntity>>> payContractDeposit(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("PAY CONTRACT DEPOSIT SUCCESS",
                paymentWalletService.payContractDeposit(contractId)));
    }

    @PostMapping("/contracts/{contractId}/expert-deposit/pay")
    public ResponseEntity<ApiResponse<PaymentActionResponse<ContractDepositEntity>>> payExpertContractDeposit(
            @PathVariable Integer contractId
    ) {
        return ResponseEntity.ok(ApiResponse.success("PAY EXPERT CONTRACT DEPOSIT SUCCESS",
                paymentWalletService.payExpertContractDeposit(contractId)));
    }

    @PostMapping("/admin/contracts/{contractId}/deposits/refund")
    public ResponseEntity<ApiResponse<List<ContractDepositEntity>>> refundContractDeposits(
            @PathVariable Integer contractId,
            @RequestBody(required = false) DepositRefundRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REFUND PARTICIPANT DEPOSITS SUCCESS",
                paymentWalletService.refundParticipantDeposits(contractId, request)));
    }

    // Note: Annotation nÃ y khai bÃ¡o API táº¡o má»›i hoáº·c gá»­i dá»¯ liá»‡u báº±ng HTTP POST.
    @PostMapping("/milestones")
    // Note: HÃ m `createMilestone` xá»­ lÃ½ má»™t API endpoint, nháº­n request, gá»i service vÃ  tráº£ káº¿t quáº£ cho client.
    public ResponseEntity<ApiResponse<MilestoneEntity>> createMilestone(@RequestBody MilestoneEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE MILESTONE SUCCESS", service.createMilestone(request)));
    }

    @PostMapping("/jobs/{jobId}/milestones")
    public ResponseEntity<ApiResponse<MilestoneEntity>> createJobMilestone(
            @PathVariable Integer jobId,
            @RequestBody MilestoneEntity request
    ) {
        return ResponseEntity.ok(ApiResponse.success("CREATE JOB MILESTONE SUCCESS",
                service.createMilestoneForJob(jobId, request)));
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
    @Operation(summary = "Deposit milestone escrow and auto-start milestone",
            description = "Business deposits milestone escrow. On success, both milestone records move to IN_PROGRESS and the execution timeline starts.")
    public ResponseEntity<ApiResponse<MilestoneEntity>> depositMilestone(@PathVariable Integer contractId, @PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("DEPOSIT MILESTONE SUCCESS", service.depositMilestoneEscrow(contractId, milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/start")
    @Operation(summary = "Start milestone compatibility endpoint",
            description = "Compatibility endpoint for older clients and legacy DEPOSITED rows. Deposit now auto-starts milestones; calling this route when already IN_PROGRESS is idempotent.")
    public ResponseEntity<ApiResponse<MilestoneEntity>> startMilestone(@PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("START MILESTONE SUCCESS", service.startMilestone(milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/deliverables")
    @Operation(summary = "Submit milestone deliverable",
            description = "The assigned Expert submits the final deliverable before the contract milestone deadline. First submissions and resubmissions are rejected after the deadline or when the milestone is OVERDUE.")
    public ResponseEntity<ApiResponse<DeliverableEntity>> submitMilestoneDeliverable(@PathVariable Integer milestoneId, @RequestBody DeliverableEntity request) {
        request.setMilestoneId(milestoneId);
        return ResponseEntity.ok(ApiResponse.success("SUBMIT DELIVERABLE SUCCESS", service.submitDeliverable(request)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/deliverables")
    @Operation(summary = "Submit contract milestone deliverable",
            description = "Context-rich alias for final deliverable submission. The milestone must belong to the contract.")
    public ResponseEntity<ApiResponse<DeliverableEntity>> submitContractMilestoneDeliverable(
            @PathVariable Integer contractId,
            @PathVariable Integer milestoneId,
            @RequestBody DeliverableEntity request
    ) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT CONTRACT DELIVERABLE SUCCESS",
                service.submitDeliverable(contractId, milestoneId, request)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/progress-reports")
    public ResponseEntity<ApiResponse<MilestoneProgressReportEntity>> submitProgressReport(@PathVariable Integer contractId, @PathVariable Integer milestoneId, @RequestBody ProgressReportRequest request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT PROGRESS REPORT SUCCESS", service.submitProgressReport(contractId, milestoneId, request)));
    }

    @PostMapping("/milestones/{milestoneId}/source-code-file")
    @Operation(summary = "Upload milestone source-code ZIP",
            description = "The assigned Expert uploads a ZIP source archive before the contract milestone deadline. Upload is rejected after the deadline or when the milestone is OVERDUE.")
    public ResponseEntity<ApiResponse<String>> uploadMilestoneSourceCode(
            @PathVariable Integer milestoneId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "UPLOAD MILESTONE SOURCE CODE SUCCESS",
                service.uploadMilestoneSourceCode(milestoneId, file)
        ));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/source-code-file")
    @Operation(summary = "Upload contract milestone source-code ZIP",
            description = "Context-rich alias for uploading source code. The milestone must belong to the contract.")
    public ResponseEntity<ApiResponse<String>> uploadContractMilestoneSourceCode(
            @PathVariable Integer contractId,
            @PathVariable Integer milestoneId,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "UPLOAD CONTRACT MILESTONE SOURCE CODE SUCCESS",
                service.uploadMilestoneSourceCode(contractId, milestoneId, file)
        ));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/progress-report-request")
    public ResponseEntity<ApiResponse<MilestoneProgressReportRequestEntity>> requestProgressReport(
            @PathVariable Integer contractId, @PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("REQUEST PROGRESS REPORT SUCCESS",
                service.requestProgressReport(contractId, milestoneId)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/acknowledge")
    public ResponseEntity<ApiResponse<MilestoneProgressReportEntity>> acknowledgeProgressReport(
            @PathVariable Integer contractId, @PathVariable Integer milestoneId,
            @PathVariable Long progressReportId) {
        return ResponseEntity.ok(ApiResponse.success("PROGRESS REPORT ACKNOWLEDGED",
                service.acknowledgeProgressReport(contractId, milestoneId, progressReportId)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/progress-reports/{progressReportId}/feedback")
    public ResponseEntity<ApiResponse<MilestoneProgressReportEntity>> feedbackProgressReport(
            @PathVariable Integer contractId, @PathVariable Integer milestoneId,
            @PathVariable Long progressReportId, @RequestBody ProgressReportFeedbackRequest request) {
        return ResponseEntity.ok(ApiResponse.success("PROGRESS REPORT FEEDBACK SUCCESS",
                service.feedbackProgressReport(contractId, milestoneId, progressReportId, request)));
    }

    @PostMapping("/contracts/{contractId}/milestones/check-overdue")
    public ResponseEntity<ApiResponse<List<MilestoneEntity>>> checkOverdue(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("CHECK OVERDUE SUCCESS", service.markOverdueMilestones(contractId)));
    }

    @GetMapping("/contracts/{contractId}/milestones/{milestoneId}/progress-reports")
    public ResponseEntity<ApiResponse<List<MilestoneProgressReportEntity>>> listProgressReports(@PathVariable Integer contractId, @PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("LIST PROGRESS REPORT SUCCESS", service.listProgressReports(contractId, milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/approve")
    public ResponseEntity<ApiResponse<MilestoneEntity>> approveMilestone(@PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE MILESTONE SUCCESS", service.approveMilestone(milestoneId)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/approve")
    public ResponseEntity<ApiResponse<MilestoneEntity>> approveContractMilestone(
            @PathVariable Integer contractId,
            @PathVariable Integer milestoneId
    ) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE CONTRACT MILESTONE SUCCESS",
                service.approveMilestone(contractId, milestoneId)));
    }

    @PostMapping("/milestones/{milestoneId}/reject")
    public ResponseEntity<ApiResponse<MilestoneEntity>> rejectMilestone(
            @PathVariable Integer milestoneId,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) RejectMilestoneRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REJECT MILESTONE SUCCESS",
                service.rejectMilestone(milestoneId, request, reason)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/reject")
    public ResponseEntity<ApiResponse<MilestoneEntity>> rejectContractMilestone(
            @PathVariable Integer contractId,
            @PathVariable Integer milestoneId,
            @RequestParam(required = false) String reason,
            @RequestBody(required = false) RejectMilestoneRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REJECT CONTRACT MILESTONE SUCCESS",
                service.rejectMilestone(contractId, milestoneId, request, reason)));
    }

    @PatchMapping("/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneEntity>> updateMilestone(@PathVariable Integer milestoneId, @RequestBody MilestoneEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE MILESTONE SUCCESS", service.updateMilestone(milestoneId, request)));
    }

    @PatchMapping("/jobs/{jobId}/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneEntity>> updateJobMilestone(
            @PathVariable Integer jobId,
            @PathVariable Integer milestoneId,
            @RequestBody MilestoneEntity request
    ) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE JOB MILESTONE SUCCESS",
                service.updateMilestoneForJob(jobId, milestoneId, request)));
    }

    @PostMapping("/milestones/{milestoneId}/disputes")
    // Chá»©c nÄƒng 1: Nháº­n request táº¡o há»“ sÆ¡ tranh cháº¥p cho milestone.
    public ResponseEntity<ApiResponse<DisputeEntity>> initiateMilestoneDispute(@PathVariable Integer milestoneId, @RequestParam Integer contractId, @RequestParam(required = false) String initiatedBy, @RequestParam(required = false) String initiationType, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("INITIATE DISPUTE SUCCESS", service.initiateDispute(contractId, milestoneId, initiatedBy, initiationType, reason)));
    }

    @PostMapping("/contracts/{contractId}/milestones/{milestoneId}/disputes")
    public ResponseEntity<ApiResponse<DisputeEntity>> initiateContractMilestoneDispute(
            @PathVariable Integer contractId,
            @PathVariable Integer milestoneId,
            @RequestParam(required = false) String initiatedBy,
            @RequestParam(required = false) String initiationType,
            @RequestParam(required = false) String reason
    ) {
        return ResponseEntity.ok(ApiResponse.success("INITIATE CONTRACT MILESTONE DISPUTE SUCCESS",
                service.initiateDispute(contractId, milestoneId, initiatedBy, initiationType, reason)));
    }

    @PostMapping("/disputes/{disputeId}/escalation-request")
    // Chá»©c nÄƒng 2: Nháº­n request yÃªu cáº§u Staff can thiá»‡p vÃ o tranh cháº¥p.
    public ResponseEntity<ApiResponse<DisputeEntity>> requestEscalation(@PathVariable Integer disputeId, @RequestParam(required = false) String reason, @RequestParam(required = false) String evidenceFile) {
        return ResponseEntity.ok(ApiResponse.success("ESCALATION REQUEST SUCCESS", service.escalateDispute(disputeId, reason, evidenceFile)));
    }

    @PostMapping("/disputes/{disputeId}/route-staff")
    // Chá»©c nÄƒng 3: Nháº­n request gÃ¡n Staff xá»­ lÃ½ tranh cháº¥p.
    public ResponseEntity<ApiResponse<DisputeEntity>> routeDisputeStaff(@PathVariable Integer disputeId, @RequestParam(required = false) Integer staffId) {
        return ResponseEntity.ok(ApiResponse.success("ROUTE DISPUTE STAFF SUCCESS", service.routeDispute(disputeId, staffId)));
    }

    @GetMapping("/disputes/{disputeId}/staff-candidates")
    // Chá»©c nÄƒng 4: Tráº£ danh sÃ¡ch Staff phÃ¹ há»£p Ä‘á»ƒ xá»­ lÃ½ tranh cháº¥p.
    public ResponseEntity<ApiResponse<List<StaffAssignmentCandidateResponse>>> listStaffCandidates(
            @PathVariable Integer disputeId) {
        return ResponseEntity.ok(ApiResponse.success("LIST STAFF CANDIDATES SUCCESS",
                service.listStaffCandidates(disputeId)));
    }

    @PostMapping("/disputes/{disputeId}/staff-decision")
    // Chá»©c nÄƒng 5: Nháº­n quyáº¿t Ä‘á»‹nh xá»­ lÃ½ tranh cháº¥p tá»« Staff.
    public ResponseEntity<ApiResponse<DisputeEntity>> staffDecideAlias(@PathVariable Integer disputeId, @RequestParam Integer expertPercent, @RequestParam(required = false) String note, @RequestParam(required = false) String staffReport) {
        return ResponseEntity.ok(ApiResponse.success("STAFF DECISION SUCCESS", service.staffDecide(disputeId, expertPercent, note, staffReport)));
    }

    @PostMapping("/disputes/{disputeId}/execute-settlement")
    // Chá»©c nÄƒng 6: Nháº­n request thá»±c hiá»‡n quyáº¿t toÃ¡n tranh cháº¥p theo quyáº¿t Ä‘á»‹nh Staff.
    public ResponseEntity<ApiResponse<DisputeEntity>> executeDisputeSettlementAlias(@PathVariable Integer disputeId) {
        return ResponseEntity.ok(ApiResponse.success("EXECUTE DISPUTE SETTLEMENT SUCCESS", service.executeDisputeSettlement(disputeId)));
    }

    @PostMapping("/disputes/{disputeId}/cancel")
    // Chá»©c nÄƒng 7: Nháº­n request rÃºt hoáº·c há»§y há»“ sÆ¡ tranh cháº¥p.
    public ResponseEntity<ApiResponse<DisputeEntity>> cancelDispute(@PathVariable Integer disputeId, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("CANCEL DISPUTE SUCCESS", service.cancelDispute(disputeId, reason)));
    }

    @PostMapping("/disputes/staff-sla-escalate")
    // Chá»©c nÄƒng 8: Nháº­n request Ä‘Ã¡nh dáº¥u tranh cháº¥p quÃ¡ háº¡n SLA Staff.
    public ResponseEntity<ApiResponse<List<DisputeEntity>>> escalateOverdueStaffDisputes() {
        return ResponseEntity.ok(ApiResponse.success("ESCALATE STAFF DISPUTE SLA SUCCESS",
                service.escalateOverdueStaffDisputes()));
    }

    @PostMapping("/contracts/{contractId}/change-requests")
    public ResponseEntity<ApiResponse<ContractChangeRequestEntity>> requestContractChange(
            @PathVariable Integer contractId,
            @RequestBody ContractChangeRequestRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REQUEST CONTRACT CHANGE SUCCESS",
                service.requestContractChange(contractId, request)));
    }

    @GetMapping("/contracts/{contractId}/change-requests")
    public ResponseEntity<ApiResponse<List<ContractChangeRequestEntity>>> listContractChangeRequests(
            @PathVariable Integer contractId
    ) {
        return ResponseEntity.ok(ApiResponse.success("LIST CONTRACT CHANGE REQUESTS SUCCESS",
                service.listContractChangeRequests(contractId)));
    }

    @PostMapping("/contracts/{contractId}/change-requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<ContractChangeRequestEntity>> acceptContractChange(
            @PathVariable Integer contractId,
            @PathVariable Integer requestId,
            @RequestBody(required = false) ContractChangeReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("ACCEPT CONTRACT CHANGE SUCCESS",
                service.acceptContractChange(contractId, requestId, request)));
    }

    @PostMapping("/contracts/{contractId}/change-requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<ContractChangeRequestEntity>> rejectContractChange(
            @PathVariable Integer contractId,
            @PathVariable Integer requestId,
            @RequestBody(required = false) ContractChangeReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REJECT CONTRACT CHANGE SUCCESS",
                service.rejectContractChange(contractId, requestId, request)));
    }

    @GetMapping("/staff/disputes")
    // Chá»©c nÄƒng 9: Tráº£ danh sÃ¡ch tranh cháº¥p Ä‘Æ°á»£c phÃ¢n cÃ´ng hoáº·c phÃ¹ há»£p vá»›i Staff.
    public ResponseEntity<ApiResponse<StaffDisputeListResponse>> listStaffDisputes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success("LIST STAFF DISPUTES SUCCESS",
                staffDisputeService.listDisputes(StaffDisputeFilter.builder()
                        .page(page).size(size).status(status).build())));
    }

    @PostMapping("/contracts/{contractId}/termination-requests")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> requestTermination(@PathVariable Integer contractId, @RequestBody TerminationRequestEntity request) {
        return ResponseEntity.ok(ApiResponse.success("REQUEST TERMINATION SUCCESS", service.requestTerminationRequest(contractId, request)));
    }

    @PostMapping("/contracts/{contractId}/immediate-termination")
    public ResponseEntity<ApiResponse<ContractEntity>> immediateTermination(
            @PathVariable Integer contractId, @RequestBody ImmediateTerminationRequest request) {
        return ResponseEntity.ok(ApiResponse.success("IMMEDIATE TERMINATION SUCCESS",
                service.immediateTerminate(contractId, request)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/accept")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> acceptTermination(
            @PathVariable Long terminationRequestId) {
        return ResponseEntity.ok(ApiResponse.success("ACCEPT TERMINATION SUCCESS",
                service.acceptBusinessTermination(terminationRequestId)));
    }

    @PostMapping("/termination-requests/{terminationRequestId}/dispute")
    public ResponseEntity<ApiResponse<TerminationRequestEntity>> disputeTermination(
            @PathVariable Long terminationRequestId, @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.success("DISPUTE TERMINATION SUCCESS",
                service.disputeBusinessTermination(terminationRequestId, reason)));
    }

    @PostMapping("/termination-requests/expire-awaiting-expert")
    public ResponseEntity<ApiResponse<List<TerminationRequestEntity>>> expireTerminationResponses() {
        return ResponseEntity.ok(ApiResponse.success("EXPIRE TERMINATION RESPONSES SUCCESS",
                service.expireAwaitingExpertTerminationResponses()));
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

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<Object>> listContracts() { return ResponseEntity.ok(ApiResponse.success("LIST CONTRACTS SUCCESS", service.listContracts())); }

    @GetMapping("/contracts/{contractId}")
    public ResponseEntity<ApiResponse<Object>> getContract(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("GET CONTRACT SUCCESS", service.getContract(contractId))); }

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
    @GetMapping("/contracts/{contractId}/milestones")
    public ResponseEntity<ApiResponse<List<ContractMilestoneViewResponse>>> listMilestones(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("LIST MILESTONES SUCCESS", service.listMilestonesByContract(contractId))); }

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
    @GetMapping("/jobs/{jobId}/milestones")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<Object>> listJobMilestones(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("LIST JOB MILESTONES SUCCESS", service.listMilestonesByJob(jobId))); }

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
    @GetMapping("/milestones/{milestoneId}/criteria")
    public ResponseEntity<ApiResponse<Object>> listCriteria(@PathVariable Integer milestoneId) { return ResponseEntity.ok(ApiResponse.success("LIST CRITERIA SUCCESS", service.listCriteriaByMilestone(milestoneId))); }

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
    @GetMapping("/milestones/{milestoneId}/deliverables")
    public ResponseEntity<ApiResponse<Object>> listDeliverables(@PathVariable Integer milestoneId) { return ResponseEntity.ok(ApiResponse.success("LIST DELIVERABLES SUCCESS", service.listDeliverablesByMilestone(milestoneId))); }

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
    @GetMapping("/contracts/{contractId}/disputes")
    // Chá»©c nÄƒng 10: Tráº£ danh sÃ¡ch tranh cháº¥p thuá»™c má»™t há»£p Ä‘á»“ng.
    public ResponseEntity<ApiResponse<Object>> listDisputes(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("LIST DISPUTES SUCCESS", service.listDisputesByContract(contractId))); }

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
    @GetMapping("/disputes/{disputeId}")
    // Chá»©c nÄƒng 11: Tráº£ chi tiáº¿t má»™t há»“ sÆ¡ tranh cháº¥p.
    public ResponseEntity<ApiResponse<DisputeEntity>> getDispute(@PathVariable Integer disputeId) { return ResponseEntity.ok(ApiResponse.success("GET DISPUTE SUCCESS", service.getDispute(disputeId))); }

    // Note: Annotation nÃ y khai bÃ¡o API Ä‘á»c dá»¯ liá»‡u báº±ng HTTP GET.
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
