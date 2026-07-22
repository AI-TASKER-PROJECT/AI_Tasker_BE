package com.aitasker.be.dto.sow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Calculated whole-VND allocation for one generated milestone.")
public class MilestoneBudgetAllocationDto {
    private Integer milestoneIndex;
    private BigDecimal referenceBudget;
    @Schema(description = "Amount frontend maps to milestones[].fundsAllocated when creating the Job.")
    private BigDecimal fundsAllocated;
}
