package com.aitasker.be.dto.sow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reference weight for one generated milestone before Job creation.")
public class MilestoneBudgetReferenceDto {
    @NotNull(message = "milestoneIndex khong duoc rong")
    @PositiveOrZero(message = "milestoneIndex khong duoc am")
    @Schema(example = "0")
    private Integer milestoneIndex;

    @NotNull(message = "referenceBudget khong duoc rong")
    @DecimalMin(value = "0.0", inclusive = false, message = "referenceBudget phai lon hon 0")
    @Digits(integer = 18, fraction = 0, message = "referenceBudget phai la so VND nguyen")
    @Schema(description = "Positive whole-VND weight copied from milestones[].recommendedBudget.", example = "40000000")
    private BigDecimal referenceBudget;
}
