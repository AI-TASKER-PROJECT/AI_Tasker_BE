package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // TỰ SINH CÁC METHOD CƠ BẢN
public class LoginRequest {
    @Email(message = "Email không đúng định dạng")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải tối thiểu 8 ký tự")
    private String password;
}