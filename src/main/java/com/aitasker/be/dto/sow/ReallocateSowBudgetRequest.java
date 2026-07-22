package com.aitasker.be.dto.sow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Business-selected custom SoW budget and milestone reference weights.")
public class ReallocateSowBudgetRequest {
    @NotNull(message = "selectedBudget khong duoc rong")
    @DecimalMin(value = "0.0", inclusive = false, message = "selectedBudget phai lon hon 0")
    @Digits(integer = 18, fraction = 0, message = "selectedBudget phai la so VND nguyen")
    @Schema(description = "Custom final budget selected by Business in whole VND.", example = "110000000")
    private BigDecimal selectedBudget;

    @Valid
    @NotEmpty(message = "milestones khong duoc rong")
    @Size(max = 50, message = "milestones khong duoc vuot qua 50 phan tu")
    private List<MilestoneBudgetReferenceDto> milestones;
}
