package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token khong duoc de trong")
    private String token;

    @NotBlank(message = "Password khong duoc de trong")
    @Size(min = 8, message = "Password phai toi thieu 8 ky tu")
    private String newPassword;
}
