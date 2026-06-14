package com.aitasker.be.controller.core;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.payment.CreateVNPayPaymentRequest;
import com.aitasker.be.dto.payment.CreateVNPayPaymentResponse;
import com.aitasker.be.entity.PaymentOrderEntity;
import com.aitasker.be.service.core.VNPayPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class PaymentController {
    private final VNPayPaymentService vnPayPaymentService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<CreateVNPayPaymentResponse>> createPayment(
            @RequestBody CreateVNPayPaymentRequest request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "CREATE VNPAY PAYMENT SUCCESS",
                vnPayPaymentService.createPayment(request, httpRequest)
        ));
    }

    @GetMapping("/return")
    public ResponseEntity<ApiResponse<PaymentOrderEntity>> handleReturn(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(ApiResponse.success(
                "HANDLE VNPAY RETURN SUCCESS",
                vnPayPaymentService.handleReturn(params)
        ));
    }
}
