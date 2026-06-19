package com.aitasker.be.dto.payment;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentActionResponse<T> {
    private boolean completed;
    private boolean needTopup;
    private BigDecimal currentBalance;
    private BigDecimal requiredAmount;
    private BigDecimal missingAmount;
    private String redirectUrl;
    private String message;
    private T data;
}
