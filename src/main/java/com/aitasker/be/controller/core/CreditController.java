package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.CreditPurchaseRequest;
import com.aitasker.be.dto.payment.PaymentActionResponse;
import com.aitasker.be.entity.UserQuotaEntity;
import com.aitasker.be.service.core.PaymentWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {
    private final PaymentWalletService paymentWalletService;

    @PostMapping("/job-post/purchase")
    public ResponseEntity<ApiResponse<PaymentActionResponse<UserQuotaEntity>>> purchaseJobPostCredits(
            @RequestBody CreditPurchaseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("PURCHASE JOB POST CREDITS SUCCESS",
                paymentWalletService.purchaseJobPostCredits(request)));
    }

    @PostMapping("/proposal/purchase")
    public ResponseEntity<ApiResponse<PaymentActionResponse<UserQuotaEntity>>> purchaseProposalCredits(
            @RequestBody CreditPurchaseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("PURCHASE PROPOSAL CREDITS SUCCESS",
                paymentWalletService.purchaseProposalCredits(request)));
    }
}
