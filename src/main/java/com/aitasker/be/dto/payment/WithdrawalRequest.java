package com.aitasker.be.dto.payment;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequest {
    private BigDecimal amount;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountHolder;
}
