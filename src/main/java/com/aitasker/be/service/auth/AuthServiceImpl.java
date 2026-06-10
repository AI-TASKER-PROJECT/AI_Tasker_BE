package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ResourceConflictException;
import com.aitasker.be.common.exception.UnauthorizedException;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RegisterRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.RoleRepository;
import com.aitasker.be.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailOtpService emailOtpService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.getEmail());
        if (!emailOtpService.isEmailVerified(email)) {
            throw new AppException("Email chưa xác thực OTP");
        }

        RoleEntity role = roleRepository.findByRoleName(req.getRole())
                .orElseThrow(() -> new UnauthorizedException("Role khong hop le"));

        AccountEntity account = AccountEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(req.getPassword()))
                .phone(req.getPhone())
                .fullName(req.getFullName())
                .role(role)
                .status("Pending")
                .build();

        AccountEntity saved = accountRepository.save(account);
        emailOtpService.clearVerifiedEmail(email);

        String accessToken = jwtService.generateAccessToken(saved.getEmail(), saved.getRole().getRoleName());
        String refreshToken = jwtService.generateRefreshToken(saved.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(saved.getRole().getRoleName())
                .accountStatus(saved.getStatus())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        AccountEntity account = accountRepository.findByEmailWithRole(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Sai email hoac mat khau"));

        if ("Lock".equalsIgnoreCase(account.getStatus())) {
            throw new UnauthorizedException("Tai khoan da bi khoa");
        }

        if (!passwordEncoder.matches(req.getPassword(), account.getPassword())) {
            throw new UnauthorizedException("Sai email hoac mat khau");
        }

        String accessToken = jwtService.generateAccessToken(account.getEmail(), account.getRole().getRoleName());
        String refreshToken = jwtService.generateRefreshToken(account.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .role(account.getRole().getRoleName())
                .accountStatus(account.getStatus())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .build();
    }

    @Override
    public boolean validateEmailNotExists(String email) {
        return accountRepository.existsByEmailIgnoreCase(email);
    }

    // Note: Hàm `normalizeEmail` xử lý nghiệp vụ chính, kiểm tra điều kiện và phối hợp repository/service liên quan.
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
