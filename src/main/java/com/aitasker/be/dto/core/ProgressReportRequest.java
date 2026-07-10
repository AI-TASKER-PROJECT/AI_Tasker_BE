package com.aitasker.be.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressReportRequest {
    private String content;
    private Integer percentComplete;
    private String attachmentUrl;
    private String sourceCodeUrl;
    private String demoLink;
    private String submissionNotes;
}
