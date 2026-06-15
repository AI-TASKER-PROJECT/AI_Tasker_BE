package com.aitasker.be.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateWalletTopupPaymentRequest {
    private BigDecimal amount;
    private String description;
}
