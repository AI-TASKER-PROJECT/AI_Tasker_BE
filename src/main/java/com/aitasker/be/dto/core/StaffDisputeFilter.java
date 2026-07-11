package com.aitasker.be.dto.core;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffDisputeFilter {
    private int page;
    private int size;
    private String status;
}
