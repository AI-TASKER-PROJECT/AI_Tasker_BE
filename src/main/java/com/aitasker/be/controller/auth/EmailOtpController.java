package com.aitasker.be.controller.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.auth.SendOtpRequest;
import com.aitasker.be.dto.auth.SendOtpResponse;
import com.aitasker.be.dto.auth.VerifyOtpRequest;
import com.aitasker.be.service.auth.EmailOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/email")
@RequiredArgsConstructor
public class EmailOtpController {
    private final EmailOtpService emailOtpService;

    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        SendOtpResponse response = emailOtpService.sendOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("OTP gửi thành công", response));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        boolean verified = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!verified) {
            throw new AppException("OTP thất bại!!!");
        }
        return ResponseEntity.ok(ApiResponse.success("Đã xác nhận Email!!!", null));
    }

}
