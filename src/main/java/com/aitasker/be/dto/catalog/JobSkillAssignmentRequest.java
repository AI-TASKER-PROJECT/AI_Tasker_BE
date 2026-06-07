package com.aitasker.be.dto.catalog;

import lombok.Data;

@Data
public class JobSkillAssignmentRequest {
    private Integer skillId;
    private String requiredLevel;
    private Boolean isMandatory;
    private Integer minYearsExperience;
}
