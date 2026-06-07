package com.aitasker.be.dto.admin;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.StaffEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StaffResponse {
    private Integer staffId;
    private Integer accountId;
    private String specialization;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StaffResponse from(StaffEntity staff, AccountEntity account) {
        return StaffResponse.builder()
                .staffId(staff.getStaffId())
                .accountId(staff.getAccountId())
                .specialization(staff.getSpecialization())
                .fullName(account == null ? null : account.getFullName())
                .email(account == null ? null : account.getEmail())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}
