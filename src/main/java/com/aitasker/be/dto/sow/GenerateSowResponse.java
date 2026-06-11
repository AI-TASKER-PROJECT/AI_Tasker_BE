package com.aitasker.be.dto.sow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateSowResponse {
    private Boolean needMoreInfo;
    private List<String> questions;
    private SowDto sow;
    private List<MilestoneDto> milestones;
}
