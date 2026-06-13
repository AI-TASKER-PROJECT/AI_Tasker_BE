package com.aitasker.be.dto.sow;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class GenerateSowRequest {
    @NotBlank(message = "projectTitle khong duoc rong")
    private String projectTitle;

    @NotBlank(message = "rawRequirement khong duoc rong")
    private String rawRequirement;

    @NotNull(message = "budget khong duoc rong")
    @DecimalMin(value = "0.0", inclusive = false, message = "budget phai lon hon 0")
    private BigDecimal budget;

    @NotNull(message = "duration khong duoc rong")
    @Positive(message = "duration phai lon hon 0")
    private Integer duration;

    @NotBlank(message = "durationUnit khong duoc rong")
    private String durationUnit;

    private List<String> supportFields;
    private List<String> requiredSkills;
}
