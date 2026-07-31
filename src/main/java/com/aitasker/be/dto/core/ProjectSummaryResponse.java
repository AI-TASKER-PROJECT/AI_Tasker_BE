package com.aitasker.be.dto.core;

import com.aitasker.be.entity.SowEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectSummaryResponse {
    private Integer contractId;
    private Integer jobId;
    private String projectTitle;
    private String projectDescription;
    private String contractScope;
    private String structuredSow;
    private SowEntity sow;
    private String status;
    private BigDecimal totalBudget;
    private Integer timelineDays;
    private Integer businessId;
    private String businessName;
    private Integer expertId;
    private String expertName;
    private Integer domainId;
    private String domainName;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<ProjectSummaryMilestoneResponse> milestones;
}
