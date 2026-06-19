package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.QuotaResponse;
import com.aitasker.be.service.core.PaymentWalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserQuotaController {
    private final PaymentWalletService paymentWalletService;

    @GetMapping("/quota")
    public ResponseEntity<ApiResponse<QuotaResponse>> currentQuota() {
        return ResponseEntity.ok(ApiResponse.success("CURRENT QUOTA SUCCESS", paymentWalletService.currentQuota()));
    }
}
