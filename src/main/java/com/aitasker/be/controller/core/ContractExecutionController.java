/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/ContractExecutionController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.core.ContractMilestoneViewResponse;
import com.aitasker.be.dto.payment.DepositRefundRequest;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.entity.*;
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

    @PostMapping("/contracts/{contractId}/terminate")
    // Note: Hàm `terminate` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<ContractEntity>> terminate(@PathVariable Integer contractId, @RequestParam String reason) {
        return ResponseEntity.ok(ApiResponse.success("TERMINATE CONTRACT SUCCESS", service.terminateContract(contractId, reason)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/milestones")
    // Note: Hàm `createMilestone` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<MilestoneEntity>> createMilestone(@RequestBody MilestoneEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE MILESTONE SUCCESS", service.createMilestone(request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/criteria")
    // Note: Hàm `createCriteria` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AcceptanceCriteriaEntity>> createCriteria(@RequestBody AcceptanceCriteriaEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE ACCEPTANCE CRITERIA SUCCESS", service.createCriteria(request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/deliverables")
    // Note: Hàm `submitDeliverable` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<DeliverableEntity>> submitDeliverable(@RequestBody DeliverableEntity request) {
        return ResponseEntity.ok(ApiResponse.success("SUBMIT DELIVERABLE SUCCESS", service.submitDeliverable(request)));
    }

    @PostMapping("/milestones/{milestoneId}/complete")
    public ResponseEntity<ApiResponse<MilestoneEntity>> completeMilestone(@PathVariable Integer milestoneId) {
        return ResponseEntity.ok(ApiResponse.success("COMPLETE MILESTONE SUCCESS", service.completeMilestone(milestoneId)));
    }

    @PatchMapping("/milestones/{milestoneId}")
    public ResponseEntity<ApiResponse<MilestoneEntity>> updateMilestone(@PathVariable Integer milestoneId, @RequestBody MilestoneEntity request) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE MILESTONE SUCCESS", service.updateMilestone(milestoneId, request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/transactions")
    // Note: Hàm `createTransaction` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<TransactionEntity>> createTransaction(@RequestBody TransactionEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE TRANSACTION SUCCESS", service.createTransaction(request)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/disputes")
    // Note: Hàm `createDispute` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<DisputeEntity>> createDispute(@RequestBody DisputeEntity request) {
        return ResponseEntity.ok(ApiResponse.success("CREATE DISPUTE SUCCESS", service.createDispute(request)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/transactions/{transactionId}/status")
    // Note: Hàm `updateTransactionStatus` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<TransactionEntity>> updateTransactionStatus(@PathVariable Long transactionId, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("UPDATE TRANSACTION STATUS SUCCESS", service.updateTransactionStatus(transactionId, status)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/disputes/{disputeId}/assign")
    // Note: Hàm `assignDispute` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<DisputeEntity>> assignDispute(@PathVariable Integer disputeId, @RequestParam Integer staffId) {
        return ResponseEntity.ok(ApiResponse.success("ASSIGN DISPUTE SUCCESS", service.assignDispute(disputeId, staffId)));
    }

    // Note: Annotation này khai báo API cập nhật một phần dữ liệu bằng HTTP PATCH.
    @PatchMapping("/disputes/{disputeId}/resolve")
    // Note: Hàm `resolveDispute` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<DisputeEntity>> resolveDispute(@PathVariable Integer disputeId, @RequestParam String proposedAction) {
        return ResponseEntity.ok(ApiResponse.success("RESOLVE DISPUTE SUCCESS", service.resolveDispute(disputeId, proposedAction)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/milestones/sla-auto-approve")
    // Note: Hàm `runSlaAutoApprove` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<Object>> runSlaAutoApprove() {
        return ResponseEntity.ok(ApiResponse.success("RUN SLA AUTO APPROVE SUCCESS", service.runSlaAutoApprove()));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/disputes/{disputeId}/demo-testing")
    // Note: Hàm `recordDemoTesting` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<DisputeEntity>> recordDemoTesting(@PathVariable Integer disputeId, @RequestParam String testResult) {
        return ResponseEntity.ok(ApiResponse.success("RECORD DEMO TESTING SUCCESS", service.recordDemoTesting(disputeId, testResult)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/disputes/{disputeId}/technical-report")
    public ResponseEntity<ApiResponse<DisputeEntity>> issueTechnicalReport(
            // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
            @PathVariable Integer disputeId,
            // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
            @RequestParam String reportContent,
            // Note: Annotation này lấy query parameter đưa vào tham số hàm.
            @RequestParam(required = false) String proposedAction) {
        return ResponseEntity.ok(ApiResponse.success("ISSUE TECHNICAL REPORT SUCCESS", service.issueTechnicalReport(disputeId, reportContent, proposedAction)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/transactions/{transactionId}/webhook")
    public ResponseEntity<ApiResponse<TransactionEntity>> processPaymentWebhook(
            // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
            @PathVariable Long transactionId,
            // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
            @RequestParam String paymentStatus,
            // Note: Annotation này lấy query parameter đưa vào tham số hàm.
            @RequestParam(required = false) String bankTxCode,
            // Note: Annotation này lấy query parameter đưa vào tham số hàm.
            @RequestParam(required = false) String receiptImgUrl) {
        return ResponseEntity.ok(ApiResponse.success("PROCESS PAYMENT WEBHOOK SUCCESS",
                service.processPaymentWebhook(transactionId, paymentStatus, bankTxCode, receiptImgUrl)));
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
    @GetMapping("/milestones/{milestoneId}/transactions")
    public ResponseEntity<ApiResponse<Object>> listTransactions(@PathVariable Integer milestoneId) { return ResponseEntity.ok(ApiResponse.success("LIST TRANSACTIONS SUCCESS", service.listTransactionsByMilestone(milestoneId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/contracts/{contractId}/disputes")
    public ResponseEntity<ApiResponse<Object>> listDisputes(@PathVariable Integer contractId) { return ResponseEntity.ok(ApiResponse.success("LIST DISPUTES SUCCESS", service.listDisputesByContract(contractId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/disputes/{disputeId}")
    public ResponseEntity<ApiResponse<DisputeEntity>> getDispute(@PathVariable Integer disputeId) { return ResponseEntity.ok(ApiResponse.success("GET DISPUTE SUCCESS", service.getDispute(disputeId))); }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/jobs/{jobId}/matching")
    public ResponseEntity<ApiResponse<Object>> matching(@PathVariable Integer jobId) { return ResponseEntity.ok(ApiResponse.success("MATCHING SUCCESS", service.matchingByKeyword(jobId))); }
}
