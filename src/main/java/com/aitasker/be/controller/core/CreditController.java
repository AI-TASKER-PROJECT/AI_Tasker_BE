/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/CreditController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.CreditPriceResponse;
import com.aitasker.be.dto.payment.CreditPurchaseRequest;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.entity.UserQuotaEntity;
import com.aitasker.be.service.core.PaymentWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/credits")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class CreditController {
    private final PaymentWalletService paymentWalletService;

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/job-post/purchase")
    // Note: Ham `purchaseJobPostCredits` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<PaymentActionResponse<UserQuotaEntity>>> purchaseJobPostCredits(
            // Note: Annotation nay bind JSON request body vao object Java.
            @RequestBody CreditPurchaseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("PURCHASE JOB POST CREDITS SUCCESS",
                paymentWalletService.purchaseJobPostCredits(request)));
    }

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/proposal/purchase")
    // Note: Ham `purchaseProposalCredits` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<PaymentActionResponse<UserQuotaEntity>>> purchaseProposalCredits(
            // Note: Annotation nay bind JSON request body vao object Java.
            @RequestBody CreditPurchaseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("PURCHASE PROPOSAL CREDITS SUCCESS",
                paymentWalletService.purchaseProposalCredits(request)));
    }

    @GetMapping("/prices")
    public ResponseEntity<ApiResponse<CreditPriceResponse>> getCreditPrices() {
        return ResponseEntity.ok(ApiResponse.success("GET CREDIT PRICES SUCCESS",
                paymentWalletService.getCreditPrices()));
    }
}
