package com.aitasker.be.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDisputeListResponse {
    private List<AdminDisputeListItem> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
