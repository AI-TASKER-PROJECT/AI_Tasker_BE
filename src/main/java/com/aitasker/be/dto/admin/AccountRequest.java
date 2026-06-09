/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/admin/AccountRequest.java
 * Đây là file gì: File DTO mô tả dữ liệu request/response, giúp tách dữ liệu API khỏi entity database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.admin;

import lombok.Data;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho dữ liệu.
@Data
public class AccountRequest {
    private String email;
    private String password;
    private String phone;
    private String fullName;
    private String role;
    private String status;
    private String specialization;
}
