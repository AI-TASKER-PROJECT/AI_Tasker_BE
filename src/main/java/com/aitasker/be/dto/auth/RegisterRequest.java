package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // TỰ SINH CÁC METHOD CƠ BẢN
public class RegisterRequest {
    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 8)
    private String password;

    @NotBlank private String fullName;
    @NotBlank private String phone;
    @NotBlank private String role;
}
