package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendOtpRequest {
    @Email
    @NotBlank
    private String email;
}
