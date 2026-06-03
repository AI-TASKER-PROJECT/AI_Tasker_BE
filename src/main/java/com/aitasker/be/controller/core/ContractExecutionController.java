package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.*;
import com.aitasker.be.service.core.ContractExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ContractExecutionController {
    private final ContractExecutionService service;

    @PostMapping("/contracts/from-proposals/{proposalId}")
    public ResponseEntity<ApiResponse<ContractEntity>> createDraft(@PathVariable Integer proposalId, @RequestBody ContractEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE CONTRACT DRAFT SUCCESS", service.createDraftFromProposal(proposalId, request)));
    }

    @PostMapping("/contracts/change-requests")
    public ResponseEntity<ApiResponse<ContractChangeRequestEntity>> requestChange(@RequestBody ContractChangeRequestEntity request) {
        return ResponseEntity.ok(ApiResponse.success("REQUEST CONTRACT CHANGE SUCCESS", service.requestChange(request)));
    }

    @PostMapping("/contracts/{contractId}/activate")
    public ResponseEntity<ApiResponse<ContractEntity>> activate(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("ACTIVATE CONTRACT SUCCESS", service.activateContract(contractId)));
    }

    @PostMapping("/contracts/{contractId}/nda-sign")
    public ResponseEntity<ApiResponse<ContractEntity>> signNda(@PathVariable Integer contractId) {
        return ResponseEntity.ok(ApiResponse.success("SIGN NDA SUCCESS", service.signNda(contractId)));
    }

    @PostMapping("/contracts/{contractId}/terminate")
    public ResponseEntity<ApiResponse<ContractEntity>> terminate(@PathVariable Integer contractId, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success("TERMINATE CONTRACT SUCCESS", service.terminateContract(contractId, reason)));
    }

    @PostMapping("/milestones")
    public ResponseEntity<ApiResponse<MilestoneEntity>> createMilestone(@RequestBody MilestoneEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE MILESTONE SUCCESS", service.createMilestone(request)));
    }

    @PostMapping("/criteria")
    public ResponseEntity<ApiResponse<AcceptanceCriteriaEntity>> createCriteria(@RequestBody AcceptanceCriteriaEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE ACCEPTANCE CRITERIA SUCCESS", service.createCriteria(request)));
    }

    @PostMapping("/deliverables")
    public ResponseEntity<ApiResponse<DeliverableEntity>> submitDeliverable(@RequestBody DeliverableEntity request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT DELIVERABLE SUCCESS", service.submitDeliverable(request)));
    }

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<TransactionEntity>> createTransaction(@RequestBody TransactionEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE TRANSACTION SUCCESS", service.createTransaction(request)));
    }

    @PostMapping("/invoices")
    public ResponseEntity<ApiResponse<InvoiceEntity>> createInvoice(@RequestBody InvoiceEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE INVOICE SUCCESS", service.createInvoice(request)));
    }

    @PostMapping("/disputes")
    public ResponseEntity<ApiResponse<DisputeEntity>> createDispute(@RequestBody DisputeEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE DISPUTE SUCCESS", service.createDispute(request)));
    }

    @PatchMapping("/transactions/{transactionId}/status")
    public ResponseEntity<ApiResponse<TransactionEntity>> updateTransactionStatus(@PathVariable Long transactionId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE TRANSACTION STATUS SUCCESS", service.updateTransactionStatus(transactionId, status)));
    }

    @PatchMapping("/disputes/{disputeId}/assign")
    public ResponseEntity<ApiResponse<DisputeEntity>> assignDispute(@PathVariable Integer disputeId, @RequestParam Integer staffId) {
        return ResponseEntity.ok(ApiResponse.success("ASSIGN DISPUTE SUCCESS", service.assignDispute(disputeId, staffId)));
    }

    @PatchMapping("/disputes/{disputeId}/resolve")
    public ResponseEntity<ApiResponse<DisputeEntity>> resolveDispute(@PathVariable Integer disputeId, @RequestParam String proposedAction) {
        return ResponseEntity.ok(ApiResponse.success("RESOLVE DISPUTE SUCCESS", service.resolveDispute(disputeId, proposedAction)));
    }

    @PostMapping("/milestones/sla-auto-approve")
    public ResponseEntity<ApiResponse<Object>> runSlaAutoApprove() {
        return ResponseEntity.ok(ApiResponse.success("RUN SLA AUTO APPROVE SUCCESS", service.runSlaAutoApprove()));
    }

    @PostMapping("/disputes/{disputeId}/demo-testing")
    public ResponseEntity<ApiResponse<DisputeEntity>> recordDemoTesting(@PathVariable Integer disputeId, @RequestParam String testResult) {
        return ResponseEntity.ok(ApiResponse.success("RECORD DEMO TESTING SUCCESS", service.recordDemoTesting(disputeId, testResult)));
    }

    @PostMapping("/disputes/{disputeId}/technical-report")
    public ResponseEntity<ApiResponse<DisputeEntity>> issueTechnicalReport(
            @PathVariable Integer disputeId,
            @RequestParam String reportContent,
            @RequestParam(required = false) String proposedAction) {
        return ResponseEntity.ok(ApiResponse.success("ISSUE TECHNICAL REPORT SUCCESS", service.issueTechnicalReport(disputeId, reportContent, proposedAction)));
    }

    @PostMapping("/transactions/{transactionId}/webhook")
    public ResponseEntity<ApiResponse<TransactionEntity>> processPaymentWebhook(
            @PathVariable Long transactionId,
            @RequestParam String paymentStatus,
            @RequestParam(required = false) String bankTxCode,
            @RequestParam(required = false) String receiptImgUrl) {
        return ResponseEntity.ok(ApiResponse.success("PROCESS PAYMENT WEBHOOK SUCCESS",
                service.processPaymentWebhook(transactionId, paymentStatus, bankTxCode, receiptImgUrl)));
    }

    @GetMapping("/contracts")
    public ResponseEntity<ApiResponse<Object>> listContracts() { return ResponseEntity.ok(ApiResponse.success("LIST CONTRACTS SUCCESS", service.listContracts())); }

    @GetMapping("/contracts/{contractId}/milestones")
    public ResponseEntity<ApiResponse<Object>> listMilestones(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("LIST MILESTONES SUCCESS", service.listMilestonesByContract(contractId))); }

    @GetMapping("/milestones/{milestoneId}/criteria")
    public ResponseEntity<ApiResponse<Object>> listCriteria(@PathVariable Integer milestoneId) { return ResponseEntity.ok(ApiResponse.success("LIST CRITERIA SUCCESS", service.listCriteriaByMilestone(milestoneId))); }

    @GetMapping("/jobs/{jobId}/matching")
    public ResponseEntity<ApiResponse<Object>> matching(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("MATCHING SUCCESS", service.matchingByKeyword(jobId))); }
}
