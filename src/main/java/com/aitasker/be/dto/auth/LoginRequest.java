package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // TỰ SINH CÁC METHOD CƠ BẢN
public class LoginRequest {
    @NotBlank private String email;
    @NotBlank private String password;
}