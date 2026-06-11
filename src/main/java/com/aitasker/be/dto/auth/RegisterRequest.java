/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/auth/RegisterRequest.java
 * Đây là file gì: File DTO mô tả dữ liệu request/response, giúp tách dữ liệu API khỏi entity database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Data // TỰ SINH CÁC METHOD CƠ BẢN
public class RegisterRequest {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Email @NotBlank
    private String email;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @NotBlank @Size(min = 8)
    private String password;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @NotBlank private String fullName;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @NotBlank private String phone;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @NotBlank private String role;
}
