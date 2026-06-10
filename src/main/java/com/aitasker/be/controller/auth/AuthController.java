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

import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RegisterRequest;
import com.aitasker.be.service.auth.AuthService;

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
@SecurityRequirements
public class AuthController {
    private final AuthService authService;

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/register")
    // Note: Hàm `register` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Register success", authService.register(req)));
    }

    // Note: Annotation này khai báo API tạo mới hoặc gửi dữ liệu bằng HTTP POST.
    @PostMapping("/login")
    // Note: Hàm `login` xử lý một API endpoint, nhận request, gọi service và trả kết quả cho client.
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(ApiResponse.success("Login success", authService.login(req)));
    }

    // Note: Annotation này khai báo API đọc dữ liệu bằng HTTP GET.
    @GetMapping("/me")
    // Note: Hàm `me` trả lại thông tin tài khoản mới nhất theo JWT để frontend cập nhật trạng thái duyệt khi reload.
    public ResponseEntity<ApiResponse<AuthResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success("Current session", authService.currentSession()));
    }
}
