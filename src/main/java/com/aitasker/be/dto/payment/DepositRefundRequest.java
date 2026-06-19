package com.aitasker.be.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepositRefundRequest {
    private BigDecimal refundAmount;
    private String adminNote;
}
