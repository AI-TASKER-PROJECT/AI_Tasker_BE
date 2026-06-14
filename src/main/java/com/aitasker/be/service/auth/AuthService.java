/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/auth/AuthService.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.auth;

import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.GoogleRegisterRequest;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RegisterRequest;

public interface AuthService {
    // Note: Hàm `register` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    AuthResponse register(RegisterRequest req);
    AuthResponse googleRegister(GoogleRegisterRequest req);
    // Note: Hàm `login` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    AuthResponse login(LoginRequest req);
    // Note: Hàm `currentSession` lấy lại thông tin tài khoản hiện tại từ database để frontend cập nhật status sau khi reload.
    AuthResponse currentSession();

    boolean validateEmailNotExists(String email);
}
