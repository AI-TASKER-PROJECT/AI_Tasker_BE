package com.aitasker.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Contract security-deposit percentages configured by Admin.")
public class ContractDepositRateResponse {
    private BigDecimal businessPercentage;
    private BigDecimal expertPercentage;
}
