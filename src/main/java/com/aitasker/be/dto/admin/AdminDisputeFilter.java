package com.aitasker.be.dto.admin;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminDisputeFilter {
    private Integer page = 0;
    private Integer size = 20;
    private String status;
    private Integer assignedStaffId;
    private LocalDateTime from;
    private LocalDateTime to;
    private String q;
}
