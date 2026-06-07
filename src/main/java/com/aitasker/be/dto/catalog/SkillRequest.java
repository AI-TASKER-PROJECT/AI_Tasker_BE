package com.aitasker.be.dto.catalog;

import lombok.Data;

@Data
public class SkillRequest {
    private String skillCode;
    private String skillName;
    private String description;
    private Boolean isActive;
}
