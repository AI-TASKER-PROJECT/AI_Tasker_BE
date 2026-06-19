package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.dto.payment.WithdrawalRequest;
import com.aitasker.be.dto.payment.WithdrawalReviewRequest;
import com.aitasker.be.entity.WithdrawalRequestEntity;
import com.aitasker.be.service.core.PaymentWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WithdrawalController {
    private final PaymentWalletService paymentWalletService;

    @PostMapping("/withdrawal-requests")
    public ResponseEntity<ApiResponse<PaymentActionResponse<WithdrawalRequestEntity>>> createWithdrawalRequest(
            @RequestBody WithdrawalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("CREATE WITHDRAWAL REQUEST SUCCESS",
                paymentWalletService.createWithdrawalRequest(request)));
    }

    @GetMapping("/withdrawal-requests")
    public ResponseEntity<ApiResponse<List<WithdrawalRequestEntity>>> listMyWithdrawalRequests() {
        return ResponseEntity.ok(ApiResponse.success("LIST WITHDRAWAL REQUESTS SUCCESS",
                paymentWalletService.listMyWithdrawalRequests()));
    }

    @GetMapping("/admin/withdrawal-requests")
    public ResponseEntity<ApiResponse<List<WithdrawalRequestEntity>>> listWithdrawalRequestsForAdmin() {
        return ResponseEntity.ok(ApiResponse.success("LIST ADMIN WITHDRAWAL REQUESTS SUCCESS",
                paymentWalletService.listWithdrawalRequestsForAdmin()));
    }

    @PostMapping("/admin/withdrawal-requests/{withdrawalId}/approve")
    public ResponseEntity<ApiResponse<WithdrawalRequestEntity>> approveWithdrawal(
            @PathVariable Long withdrawalId,
            @RequestBody(required = false) WithdrawalReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE WITHDRAWAL REQUEST SUCCESS",
                paymentWalletService.approveWithdrawal(withdrawalId, request)));
    }

    @PostMapping("/admin/withdrawal-requests/{withdrawalId}/reject")
    public ResponseEntity<ApiResponse<WithdrawalRequestEntity>> rejectWithdrawal(
            @PathVariable Long withdrawalId,
            @RequestBody(required = false) WithdrawalReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REJECT WITHDRAWAL REQUEST SUCCESS",
                paymentWalletService.rejectWithdrawal(withdrawalId, request)));
    }
}
