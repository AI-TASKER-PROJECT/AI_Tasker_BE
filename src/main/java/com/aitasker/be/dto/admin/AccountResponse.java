/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/admin/AccountResponse.java
 * Đây là file gì: File DTO mô tả dữ liệu request/response, giúp tách dữ liệu API khỏi entity database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.admin;

import com.aitasker.be.entity.AccountEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho dữ liệu.
@Data
// Note: Annotation này giúp Lombok tạo builder để khởi tạo object rõ ràng hơn.
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

    // Note: Hàm `from` phục vụ tạo hoặc đọc dữ liệu truyền qua API.
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
