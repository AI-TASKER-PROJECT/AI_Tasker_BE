/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/admin/StaffResponse.java
 * Đây là file gì: File DTO mô tả dữ liệu request/response, giúp tách dữ liệu API khỏi entity database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.admin;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.StaffEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho dữ liệu.
@Data
// Note: Annotation này giúp Lombok tạo builder để khởi tạo object rõ ràng hơn.
@Builder
public class StaffResponse {
    private Integer staffId;
    private Integer accountId;
    private String specialization;
    private String fullName;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Note: Hàm `from` phục vụ tạo hoặc đọc dữ liệu truyền qua API.
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
