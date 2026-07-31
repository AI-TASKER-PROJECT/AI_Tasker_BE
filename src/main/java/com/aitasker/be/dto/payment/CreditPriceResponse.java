package com.aitasker.be.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Current retail credit prices configured by Admin.")
public class CreditPriceResponse {
    private BigDecimal jobPostPriceVnd;
    private BigDecimal proposalPriceVnd;
}
