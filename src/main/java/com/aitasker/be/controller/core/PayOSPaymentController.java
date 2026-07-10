/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/core/PayOSPaymentController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.CreatePayOSPaymentResponse;
import com.aitasker.be.dto.payment.CreateWalletTopupPaymentRequest;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.service.core.PayOSPaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/payments/payos")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class PayOSPaymentController {
    private final PayOSPaymentService payOSPaymentService;

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/create")
    // Note: Ham `createPayment` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<CreatePayOSPaymentResponse>> createPayment(
            // Note: Annotation nay bind JSON request body vao object Java.
            @RequestBody CreateWalletTopupPaymentRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "CREATE PAYOS PAYMENT SUCCESS",
                payOSPaymentService.createPayment(request)
        ));
    }

    // Note: Annotation nay khai bao API doc du lieu bang HTTP GET.
    @GetMapping("/return")
    // Note: Annotation nay ghi ro endpoint/class nay khong ap dung security scheme mac dinh tren Swagger.
    @SecurityRequirements
    // Note: Ham `handleReturn` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<PaymentOrderEntity>> handleReturn(@RequestParam Map<String, String> params) {
        String orderCodeValue = params.get("orderCode");
        if (orderCodeValue == null || orderCodeValue.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("THIEU_ORDER_CODE"));
        }

        try {
            PaymentOrderEntity paymentOrder = payOSPaymentService.syncPaymentStatus(Long.parseLong(orderCodeValue));
            return ResponseEntity.ok(ApiResponse.success("PAYOS RETURN SUCCESS", paymentOrder));
        } catch (Exception ex) {
            return ResponseEntity.ok(ApiResponse.error(ex.getMessage()));
        }
    }

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/{orderCode}/sync")
    // Note: Ham `syncPaymentStatus` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<PaymentOrderEntity>> syncPaymentStatus(@PathVariable Long orderCode) {
        return ResponseEntity.ok(ApiResponse.success(
                "SYNC PAYOS PAYMENT SUCCESS",
                payOSPaymentService.syncPaymentStatus(orderCode)
        ));
    }

}
