/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/auth/AuthService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.auth;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.ForgotPasswordRequest;
import com.aitasker.be.dto.auth.GoogleAuthRequest;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RefreshTokenRequest;
import com.aitasker.be.dto.auth.RegisterRequest;
import com.aitasker.be.dto.auth.ResetPasswordRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse googleLogin(GoogleAuthRequest req);
    AuthResponse login(LoginRequest req);
    AuthResponse refreshToken(RefreshTokenRequest req);
    AuthResponse currentSession();

    boolean emailExists(String email);

    ApiResponse<Void> forgotPassword(ForgotPasswordRequest req);

    ApiResponse<Void> resetPassword(ResetPasswordRequest req);
}
