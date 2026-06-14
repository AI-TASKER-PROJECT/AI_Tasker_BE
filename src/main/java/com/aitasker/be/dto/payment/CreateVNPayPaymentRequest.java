package com.aitasker.be.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateVNPayPaymentRequest {
    private Long accountId;
    private Long businessId;
    private Long jobId;
    private Long milestoneId;
    private BigDecimal amount;
    private String description;
}
