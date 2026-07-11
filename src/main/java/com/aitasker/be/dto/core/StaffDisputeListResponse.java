package com.aitasker.be.dto.core;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StaffDisputeListResponse {
    private List<StaffDisputeListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
