package com.aitasker.be.dto.core;

import lombok.Data;

@Data
public class ImmediateTerminationRequest {
    private String reason;
    private Boolean confirmedPenalty;
}
