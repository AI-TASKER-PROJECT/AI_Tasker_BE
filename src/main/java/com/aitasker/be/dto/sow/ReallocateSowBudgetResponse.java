package com.aitasker.be.dto.sow;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Exact custom-budget allocation calculated without an AI or database call.")
public class ReallocateSowBudgetResponse {
    @Schema(example = "VND")
    private String currency;
    private BigDecimal selectedBudget;
    @Schema(description = "Exact sum of allocations; always equals selectedBudget.")
    private BigDecimal allocationTotal;
    private List<MilestoneBudgetAllocationDto> allocations;
}
