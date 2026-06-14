package com.aitasker.be.dto.payment;

import com.aitasker.be.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreateVNPayPaymentResponse {
    private String paymentUrl;
    private String vnpTxnRef;
    private BigDecimal amount;
    private PaymentStatus status;
}
