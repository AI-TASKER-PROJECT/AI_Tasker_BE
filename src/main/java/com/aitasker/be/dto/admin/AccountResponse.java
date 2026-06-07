package com.aitasker.be.dto.admin;

import com.aitasker.be.entity.AccountEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountResponse {
    private Integer accountId;
    private String email;
    private String phone;
    private String fullName;
    private String role;
    private String status;
    private String specialization;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse from(AccountEntity account) {
        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .phone(account.getPhone())
                .fullName(account.getFullName())
                .role(account.getRole() == null ? null : account.getRole().getRoleName())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
