package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.CreatePayOSPaymentResponse;
import com.aitasker.be.dto.payment.CreateWalletTopupPaymentRequest;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.service.core.PayOSPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

@RestController
@RequestMapping("/api/payments/payos")
@RequiredArgsConstructor
public class PayOSPaymentController {
    private final PayOSPaymentService payOSPaymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreatePayOSPaymentResponse>> createPayment(
            @RequestBody CreateWalletTopupPaymentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "CREATE PAYOS PAYMENT SUCCESS",
                payOSPaymentService.createPayment(request)
        ));
    }

    @PostMapping("/webhook")
    public ResponseEntity<ApiResponse<PaymentOrderEntity>> handleWebhook(@RequestBody Webhook webhook) {
        return ResponseEntity.ok(ApiResponse.success(
                "HANDLE PAYOS WEBHOOK SUCCESS",
                payOSPaymentService.handleWebhook(webhook)
        ));
    }
}
