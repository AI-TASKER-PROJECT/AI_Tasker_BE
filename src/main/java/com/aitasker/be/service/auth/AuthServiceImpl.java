package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ResourceConflictException;
import com.aitasker.be.common.exception.UnauthorizedException;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.GoogleRegisterRequest;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RegisterRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.RoleRepository;
import com.aitasker.be.security.SecurityUtils;
import com.aitasker.be.security.jwt.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailOtpService emailOtpService;

    @Value("${google.client-id:}")
    private String googleClientId;

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
    @Transactional
    public AuthResponse googleRegister(GoogleRegisterRequest req) {
        GoogleIdToken.Payload payload = verifyGoogleCredential(req.getCredential());
        String email = normalizeEmail(payload.getEmail());
        String googleName = (String) payload.get("name");
        String fullName = req.getFullName() == null || req.getFullName().isBlank()
                ? googleName
                : req.getFullName().trim();

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new UnauthorizedException("Google email chua duoc xac thuc");
        }
        var existingAccount = accountRepository.findByEmailWithRole(email);
        if (existingAccount.isPresent()) {
            AccountEntity account = existingAccount.get();
            if ("Lock".equalsIgnoreCase(account.getStatus())) {
                throw new UnauthorizedException("Tai khoan da bi khoa");
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

        RoleEntity role = roleRepository.findByRoleName(req.getRole())
                .orElseThrow(() -> new UnauthorizedException("Role khong hop le"));

        AccountEntity account = AccountEntity.builder()
                .email(email)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .phone(req.getPhone())
                .fullName(fullName == null || fullName.isBlank() ? email : fullName)
                .role(role)
                .status("Pending")
                .emailVerified(true)
                .build();

        AccountEntity saved = accountRepository.save(account);
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
    // Note: Hàm `currentSession` đọc account mới nhất theo JWT hiện tại để frontend không cần đăng xuất rồi đăng nhập lại khi status đổi.
    @Override
    @Transactional(readOnly = true)
    public AuthResponse currentSession() {
        AccountEntity account = accountRepository.findByEmailWithRole(SecurityUtils.getCurrentEmail())
                .orElseThrow(() -> new UnauthorizedException("Tai khoan khong hop le"));
        return AuthResponse.builder()
                .role(account.getRole().getRoleName())
                .accountStatus(account.getStatus())
                .email(account.getEmail())
                .fullName(account.getFullName())
                .build();
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private GoogleIdToken.Payload verifyGoogleCredential(String credential) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new UnauthorizedException("Google client id chua duoc cau hinh");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(credential);
            if (idToken == null) {
                throw new UnauthorizedException("Google credential khong hop le");
            }
            return idToken.getPayload();
        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            throw new UnauthorizedException("Khong the xac thuc Google credential");
        }
    }
}
