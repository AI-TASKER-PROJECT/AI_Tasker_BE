/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/auth/AuthController.java
 * Đây là file gì: File controller nhận request HTTP, gọi service phù hợp và trả response cho client.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.controller.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.config.OpenApiConfig;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.ForgotPasswordRequest;
import com.aitasker.be.dto.auth.GoogleAuthRequest;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RefreshTokenRequest;
import com.aitasker.be.dto.auth.RegisterRequest;
import com.aitasker.be.dto.auth.ResetPasswordRequest;
import com.aitasker.be.service.auth.AuthService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Note: Annotation này biến class thành REST controller để nhận request và trả JSON.
@RestController
// Note: Annotation này đặt prefix đường dẫn API cho controller hoặc method.
@RequestMapping("/api/auth")
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
public class AuthController {
    private final AuthService authService;

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/register")
    @SecurityRequirements
    // Note: Hàm `register` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Register success", authService.register(req)));
    }

    @PostMapping("/google/login")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleAuthRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Google login success", authService.googleLogin(req)));
    }

    @PostMapping("/google/register")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> googleRegister(@Valid @RequestBody GoogleAuthRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Google register success", authService.googleLogin(req)));
    }

    @GetMapping("/check-email")
    @SecurityRequirements
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.emailExists(email));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/login")
    @SecurityRequirements
    // Note: Hàm `login` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Login success", authService.login(req)));
    }

    @PostMapping("/refresh")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Refresh token success", authService.refreshToken(req)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/me")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
    // Note: Hàm `me` trả lại thông tin tài khoản mới nhất theo JWT để frontend cập nhật trạng thái duyệt khi reload.
    public ResponseEntity<ApiResponse<AuthResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success("Current session", authService.currentSession()));
    }

    @PostMapping("/forgot-password")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(authService.forgotPassword(req));
    }

    @PostMapping("/reset-password")
    @SecurityRequirements
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        return ResponseEntity.ok(authService.resetPassword(req));
    }
}
