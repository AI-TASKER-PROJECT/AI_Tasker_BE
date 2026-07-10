/*
 * NOTE FILE: src/main/java/com/aitasker/be/controller/auth/EmailOtpController.java
 * Day la file gi: File controller nhan request HTTP, goi service phu hop va tra response cho client.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.controller.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.auth.SendOtpRequest;
import com.aitasker.be.dto.auth.SendOtpResponse;
import com.aitasker.be.dto.auth.VerifyOtpRequest;
import com.aitasker.be.service.auth.EmailOtpService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Note: Annotation nay bien class thanh REST controller de nhan request va tra JSON.
@RestController
// Note: Annotation nay dat prefix duong dan API cho controller hoac method.
@RequestMapping("/api/auth/email")
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
// Note: Annotation nay ghi ro endpoint/class nay khong ap dung security scheme mac dinh tren Swagger.
@SecurityRequirements
public class EmailOtpController {
    private final EmailOtpService emailOtpService;

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/send-otp")
    // Note: Ham `sendOtp` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        SendOtpResponse response = emailOtpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP gửi thành công", response));
    }

    // Note: Annotation nay khai bao API tao moi hoac gui du lieu bang HTTP POST.
    @PostMapping("/verify-otp")
    // Note: Ham `verifyOtp` xu ly mot API endpoint, nhan request, goi service va tra ket qua cho client.
    public ResponseEntity<ApiResponse<Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean verified = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!verified) {
            throw new AppException("OTP thất bại!!!");
        }
        return ResponseEntity.ok(ApiResponse.success("Đã xác nhận Email!!!", null));
    }

}
