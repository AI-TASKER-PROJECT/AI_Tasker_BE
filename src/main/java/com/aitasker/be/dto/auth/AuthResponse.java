package com.aitasker.be.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // TỰ SINH CÁC METHOD CƠ BẢN
@Builder // TẠO OBJECT RESPONSE THEO KIỂU BUILDER PATTERN
@AllArgsConstructor @NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String role;
    private String email;
    private String fullName;
}
