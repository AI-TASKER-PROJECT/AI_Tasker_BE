package com.aitasker.be.dto.core;

import com.aitasker.be.entity.DeliverableEntity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProjectSummaryMilestoneResponse {
    private Integer contractMilestoneId;
    private Integer milestoneId;
    private String milestoneName;
    private String description;
    private BigDecimal budget;
    private Integer orderIndex;
    private String status;
    private Integer duration;
    private String durationUnit;
    private String deliverableExpectation;
    private List<String> acceptanceCriteria;
    private DeliverableEntity finalDeliverable;
    private LocalDateTime completedAt;
}
