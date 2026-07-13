package com.aitasker.be.dto.admin;

import lombok.Data;

@Data
public class SystemSettingRequest {
    private String settingKey;
    private String settingValue;
    private String valueType;
    private String description;
    private Boolean isActive;
}
