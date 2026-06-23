/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/WithdrawalController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
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

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/v1")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class WithdrawalController {
    private final PaymentWalletService paymentWalletService;

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/withdrawal-requests")
    // Note: Ham `createWithdrawalRequest` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<PaymentActionResponse<WithdrawalRequestEntity>>> createWithdrawalRequest(
            // Note: Annotation nay bind JSON request body vao object Java.
            @RequestBody WithdrawalRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("CREATE WITHDRAWAL REQUEST SUCCESS",
                paymentWalletService.createWithdrawalRequest(request)));
    }

    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/withdrawal-requests")
    // Note: Ham `listMyWithdrawalRequests` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<List<WithdrawalRequestEntity>>> listMyWithdrawalRequests() {
        return ResponseEntity.ok(ApiResponse.success("LIST WITHDRAWAL REQUESTS SUCCESS",
                paymentWalletService.listMyWithdrawalRequests()));
    }

    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/admin/withdrawal-requests")
    // Note: Ham `listWithdrawalRequestsForAdmin` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<List<WithdrawalRequestEntity>>> listWithdrawalRequestsForAdmin() {
        return ResponseEntity.ok(ApiResponse.success("LIST ADMIN WITHDRAWAL REQUESTS SUCCESS",
                paymentWalletService.listWithdrawalRequestsForAdmin()));
    }

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/admin/withdrawal-requests/{withdrawalId}/approve")
    // Note: Ham `approveWithdrawal` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<WithdrawalRequestEntity>> approveWithdrawal(
            // Note: Annotation nay doc bien tren URL path vao tham so ham.
            @PathVariable Long withdrawalId,
            // Note: Annotation nay bind JSON request body vao object Java.
            @RequestBody(required = false) WithdrawalReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("APPROVE WITHDRAWAL REQUEST SUCCESS",
                paymentWalletService.approveWithdrawal(withdrawalId, request)));
    }

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/admin/withdrawal-requests/{withdrawalId}/reject")
    // Note: Ham `rejectWithdrawal` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<WithdrawalRequestEntity>> rejectWithdrawal(
            // Note: Annotation nay doc bien tren URL path vao tham so ham.
            @PathVariable Long withdrawalId,
            // Note: Annotation nay bind JSON request body vao object Java.
            @RequestBody(required = false) WithdrawalReviewRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("REJECT WITHDRAWAL REQUEST SUCCESS",
                paymentWalletService.rejectWithdrawal(withdrawalId, request)));
    }
}
