package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleRegisterRequest {
    @NotBlank
    private String credential;

    @NotBlank
    private String role;

    private String fullName;

    private String phone;
}
