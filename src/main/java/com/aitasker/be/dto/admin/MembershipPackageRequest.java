package com.aitasker.be.dto.admin;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MembershipPackageRequest {
    private String roleType;
    private String packageCode;
    private String packageName;
    private BigDecimal price;
    private Integer badgeDurationDays;
    private Integer jobPostQuota;
    private Integer proposalQuota;
    private Boolean recommendVisibility;
    private Boolean isActive;
}
