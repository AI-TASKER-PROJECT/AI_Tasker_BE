package com.aitasker.be.dto.core;

import lombok.Data;

@Data
public class RejectedMilestoneTerminationRequest {
    private Integer contractMilestoneId;
    private String reason;
}
