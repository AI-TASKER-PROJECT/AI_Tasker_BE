/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/auth/RefreshTokenRequest.java
 * Day la file gi: DTO nhan refresh token de cap access token moi cho frontend.
 * Muc dich note: giu request refresh token tach rieng voi login de validate dung muc dich token.
 */
package com.aitasker.be.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;
}
