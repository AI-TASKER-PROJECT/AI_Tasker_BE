package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank
    private String credential;

    private String role;

    private String fullName;

    private String phone;
}
