package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.config.PayOSProperties;
import com.aitasker.be.dto.payment.CreatePayOSPaymentResponse;
import com.aitasker.be.dto.payment.CreateWalletTopupPaymentRequest;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.entity.PaymentStatus;
import com.aitasker.be.service.core.PayOSPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/payments/payos")
@RequiredArgsConstructor
public class PayOSPaymentController {
    private final PayOSPaymentService payOSPaymentService;
    private final PayOSProperties payOSProperties;

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

    @GetMapping("/return")
    public ResponseEntity<Void> handleReturn(@RequestParam Map<String, String> params) {
        String orderCodeValue = params.get("orderCode");
        if (orderCodeValue == null || orderCodeValue.isBlank()) {
            return redirectToApp(null, PaymentStatus.FAILED, "THIEU_ORDER_CODE");
        }

        try {
            PaymentOrderEntity paymentOrder = payOSPaymentService.syncPaymentStatus(Long.parseLong(orderCodeValue));
            return redirectToApp(paymentOrder.getProviderOrderCode(), paymentOrder.getStatus(), null);
        } catch (Exception ex) {
            return redirectToApp(parseOrderCode(orderCodeValue), PaymentStatus.FAILED, ex.getMessage());
        }
    }

    @PostMapping("/{orderCode}/sync")
    public ResponseEntity<ApiResponse<PaymentOrderEntity>> syncPaymentStatus(@PathVariable Long orderCode) {
        return ResponseEntity.ok(ApiResponse.success(
                "SYNC PAYOS PAYMENT SUCCESS",
                payOSPaymentService.syncPaymentStatus(orderCode)
        ));
    }

    private ResponseEntity<Void> redirectToApp(Long orderCode, PaymentStatus status, String error) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(payOSProperties.getAppReturnUrl())
                .queryParam("paymentStatus", status.name());

        if (orderCode != null) {
            builder.queryParam("orderCode", orderCode);
        }
        if (error != null && !error.isBlank()) {
            builder.queryParam("message", error);
        }

        URI location = builder.build(true).toUri();
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(location)
                .build();
    }

    private Long parseOrderCode(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
