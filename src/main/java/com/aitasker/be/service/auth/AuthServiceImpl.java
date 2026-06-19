package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ResourceConflictException;
import com.aitasker.be.common.exception.UnauthorizedException;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.GoogleAuthRequest;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RegisterRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.repository.RoleRepository;
import com.aitasker.be.security.SecurityUtils;
import com.aitasker.be.security.jwt.JwtService;
import com.aitasker.be.service.core.PaymentWalletService;
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
    private final PaymentWalletService paymentWalletService;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalizeEmail(req.getEmail());
        if (!emailOtpService.isEmailVerified(email)) {
            throw new AppException("Email chua xac thuc OTP");
        }
        if (accountRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException("Email da ton tai");
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
                .emailVerified(true)
                .build();

        AccountEntity saved = accountRepository.save(account);
        paymentWalletService.ensureQuotaForAccount(saved);
        emailOtpService.clearVerifiedEmail(email);
        return buildAuthResponse(saved);
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest req) {
        GoogleIdToken.Payload payload = verifyGoogleCredential(req.getCredential());
        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            throw new UnauthorizedException("Google email chua duoc xac thuc");
        }

        String email = normalizeEmail(payload.getEmail());
        return accountRepository.findByEmailWithRole(email)
                .map(this::buildAuthResponse)
                .orElseGet(() -> createGoogleAccount(req, email, payload));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        AccountEntity account = accountRepository.findByEmailWithRole(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Sai email hoac mat khau"));

        if (!passwordEncoder.matches(req.getPassword(), account.getPassword())) {
            throw new UnauthorizedException("Sai email hoac mat khau");
        }

        return buildAuthResponse(account);
    }

    @Override
    public boolean emailExists(String email) {
        return accountRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }

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

    private AuthResponse createGoogleAccount(GoogleAuthRequest req, String email, GoogleIdToken.Payload payload) {
        if (req.getRole() == null || req.getRole().isBlank()) {
            throw new AppException("Role khong duoc de trong khi tao tai khoan Google moi");
        }

        RoleEntity role = roleRepository.findByRoleName(req.getRole())
                .orElseThrow(() -> new UnauthorizedException("Role khong hop le"));
        String googleName = payload.get("name") instanceof String name ? name : null;
        String fullName = req.getFullName() == null || req.getFullName().isBlank()
                ? googleName
                : req.getFullName().trim();

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
        paymentWalletService.ensureQuotaForAccount(saved);
        return buildAuthResponse(saved);
    }

    private AuthResponse buildAuthResponse(AccountEntity account) {
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
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new UnauthorizedException("Khong the xac thuc Google credential");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
