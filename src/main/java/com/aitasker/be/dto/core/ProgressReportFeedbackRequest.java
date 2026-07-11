package com.aitasker.be.dto.core;

import lombok.Data;

@Data
public class ProgressReportFeedbackRequest {
    private String category;
    private String severity;
    private Object dodItems;
    private String feedback;
    private Boolean requiresAdjustment;
}
