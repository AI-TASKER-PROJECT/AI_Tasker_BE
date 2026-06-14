package com.aitasker.be.dto.payment;

import com.aitasker.be.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CreatePayOSPaymentResponse {
    private String checkoutUrl;
    private Long orderCode;
    private BigDecimal amount;
    private PaymentStatus status;
}
