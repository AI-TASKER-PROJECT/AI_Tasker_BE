package com.aitasker.be.dto.sow;

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
public class MilestoneDto {
    private String name;
    private String description;
    private Integer duration;
    private String durationUnit;
    private BigDecimal budget;
    private List<TaskDto> tasks;
}
