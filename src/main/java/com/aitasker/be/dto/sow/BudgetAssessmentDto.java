package com.aitasker.be.dto.sow;

import com.fasterxml.jackson.annotation.JsonAlias;
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
@Schema(description = "Advisory AI budget comparison. Business remains the final budget authority.")
public class BudgetAssessmentDto {
    @Schema(example = "VND")
    private String currency;
    @Schema(description = "Budget entered by Business; backend never overwrites it.")
    private BigDecimal businessBudget;
    @JsonAlias("min")
    @Schema(description = "Advisory minimum for the generated full scope.")
    private BigDecimal estimatedMin;
    @JsonAlias("recommended")
    @Schema(description = "Advisory recommended budget for the generated full scope.")
    private BigDecimal recommendedBudget;
    @JsonAlias("max")
    @Schema(description = "Advisory upper estimate for the generated full scope.")
    private BigDecimal estimatedMax;
    @Schema(allowableValues = {"TOO_LOW", "LOW", "SUITABLE", "HIGH"})
    private String status;
    @Schema(description = "Positive difference to estimatedMin; zero when Business budget is not below it.")
    private BigDecimal gapToMinimum;
    @Schema(allowableValues = {"LOW", "MEDIUM", "HIGH"})
    private String confidence;
    @Schema(allowableValues = {"AI_ADVISORY", "AI_MILESTONE_FALLBACK"})
    private String source;
    @Schema(description = "True when UI must request explicit budget confirmation; false for HIGH because the higher Business amount remains authoritative without showing the advisory card.")
    private Boolean requiresBusinessConfirmation;
    private String message;
    private List<String> factors;
}
