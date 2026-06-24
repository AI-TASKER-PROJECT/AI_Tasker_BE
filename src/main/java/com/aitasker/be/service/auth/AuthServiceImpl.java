package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.ResourceConflictException;
import com.aitasker.be.common.exception.UnauthorizedException;
import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.ForgotPasswordRequest;
import com.aitasker.be.dto.auth.GoogleAuthRequest;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RegisterRequest;
import com.aitasker.be.dto.auth.ResetPasswordRequest;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailOtpService emailOtpService;
    private final PaymentWalletService paymentWalletService;
    private final SecurityEmailService securityEmailService;
    private final StringRedisTemplate redisTemplate;

    @Value("${google.client-id:}")
    private String googleClientId;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.password-reset-token-ttl-minutes:15}")
    private int passwordResetTokenTtlMinutes;

    @Value("${app.password-reset-rate-limit-seconds:60}")
    private int passwordResetRateLimitSeconds;

    @Value("${app.login-temporary-lock-minutes:5}")
    private int loginTemporaryLockMinutes;

    @Value("${app.login-failed-attempt-threshold:5}")
    private int loginFailedAttemptThreshold;

    private static final String RESET_TOKEN_PREFIX = "PASSWORD_RESET:";
    private static final String RESET_TOKEN_PENDING_PREFIX = "PASSWORD_RESET_PENDING:";
    private static final String RESET_RATE_PREFIX = "PASSWORD_RESET_RATE:";
    private static final String LOCK_REASON_TOO_MANY_FAILED = "TOO_MANY_FAILED_LOGIN_ATTEMPTS";
    private static final String LOCK_REASON_ADMIN_LOCKED = "ADMIN_LOCKED";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private enum LoginSecurityEventType {
        NONE,
        TEMPORARY_LOCKOUT,
        SECURITY_LOCK
    }

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
    @Transactional
    public AuthResponse login(LoginRequest req) {
        AccountEntity account = accountRepository
                .findByEmailWithRoleForUpdate(normalizeEmail(req.getEmail()))
                .orElseThrow(() -> new UnauthorizedException("Sai email hoac mat khau"));

        if (isTemporarilyLocked(account)) {
            throw new UnauthorizedException("Tai khoan dang bi tam khoa, vui long thu lai sau");
        }

        if (isSecurityLocked(account)) {
            throw new UnauthorizedException("Tai khoan da bi khoa, vui long dat lai mat khau");
        }

        if (!passwordEncoder.matches(req.getPassword(), account.getPassword())) {
            LoginSecurityEventType event = handleFailedLogin(account);
            accountRepository.save(account);
            sendSecurityEventEmail(account, event);
            throw new UnauthorizedException("Sai email hoac mat khau");
        }

        resetFailedLoginCounters(account);
        accountRepository.save(account);
        return buildAuthResponse(account);
    }

    private boolean isTemporarilyLocked(AccountEntity account) {
        return account.getLockedUntil() != null
                && account.getLockedUntil().isAfter(LocalDateTime.now());
    }

    private boolean isSecurityLocked(AccountEntity account) {
        return "Lock".equalsIgnoreCase(account.getStatus())
                && LOCK_REASON_TOO_MANY_FAILED.equals(account.getLockReason());
    }

    private LoginSecurityEventType handleFailedLogin(AccountEntity account) {
        if (account.getFailedLoginAttempts() < 0) {
            account.setFailedLoginAttempts(0);
        }

        account.setFailedLoginAttempts(account.getFailedLoginAttempts() + 1);
        account.setLastFailedLoginAt(LocalDateTime.now());

        if (account.getFailedLoginAttempts() >= loginFailedAttemptThreshold) {
            if (account.getLockoutCount() >= 1) {
                securityLockAccount(account);
                return LoginSecurityEventType.SECURITY_LOCK;
            } else {
                temporaryLockAccount(account);
                return LoginSecurityEventType.TEMPORARY_LOCKOUT;
            }
        }
        return LoginSecurityEventType.NONE;
    }

    private void temporaryLockAccount(AccountEntity account) {
        account.setLockoutCount(account.getLockoutCount() + 1);
        account.setLockedUntil(LocalDateTime.now().plusMinutes(loginTemporaryLockMinutes));
        account.setFailedLoginAttempts(0);
    }

    private void securityLockAccount(AccountEntity account) {
        account.setStatusBeforeLock(account.getStatus());
        account.setStatus("Lock");
        account.setLockReason(LOCK_REASON_TOO_MANY_FAILED);
        account.setFailedLoginAttempts(0);
    }

    private void sendSecurityEventEmail(AccountEntity account, LoginSecurityEventType event) {
        try {
            switch (event) {
                case TEMPORARY_LOCKOUT:
                    securityEmailService.sendTemporaryLockoutEmail(account);
                    break;
                case SECURITY_LOCK:
                    String token = generateResetToken();
                    redisTemplate.opsForValue().set(
                            RESET_TOKEN_PREFIX + token,
                            account.getEmail(),
                            passwordResetTokenTtlMinutes,
                            TimeUnit.MINUTES
                    );
                    String resetLink = frontendUrl + "/reset-password?token=" + token;
                    securityEmailService.sendSecurityLockEmail(account, resetLink);
                    break;
                case NONE:
                default:
                    break;
            }
        } catch (Exception e) {
            log.warn("Failed to send security email for account {}: {}",
                    account.getEmail(), e.getMessage());
        }
    }

    private void resetFailedLoginCounters(AccountEntity account) {
        account.setFailedLoginAttempts(0);
        account.setLockoutCount(0);
        account.setLockedUntil(null);
        account.setLastFailedLoginAt(null);
    }

    @Override
    public boolean emailExists(String email) {
        return accountRepository.existsByEmailIgnoreCase(normalizeEmail(email));
    }

    @Override
    public ApiResponse<Void> forgotPassword(ForgotPasswordRequest req) {
        String normalizedEmail = normalizeEmail(req.getEmail());
        String rateKey = RESET_RATE_PREFIX + normalizedEmail;

        Boolean rateLimited = redisTemplate.hasKey(rateKey);
        if (Boolean.TRUE.equals(rateLimited)) {
            return ApiResponse.success(
                    "Neu email ton tai, huong dan dat lai mat khau se duoc gui",
                    null
            );
        }

        redisTemplate.opsForValue().set(
                rateKey,
                "true",
                passwordResetRateLimitSeconds,
                TimeUnit.SECONDS
        );

        accountRepository.findByEmailWithRole(normalizedEmail).ifPresent(account -> {
            String token = generateResetToken();
            redisTemplate.opsForValue().set(
                    RESET_TOKEN_PREFIX + token,
                    account.getEmail(),
                    passwordResetTokenTtlMinutes,
                    TimeUnit.MINUTES
            );

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            try {
                securityEmailService.sendResetPasswordEmail(account, resetLink);
            } catch (Exception e) {
                log.warn("Failed to send reset password email for {}: {}",
                        account.getEmail(), e.getMessage());
            }
        });

        return ApiResponse.success(
                "Neu email ton tai, huong dan dat lai mat khau se duoc gui",
                null
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> resetPassword(ResetPasswordRequest req) {
        String tokenKey = RESET_TOKEN_PREFIX + req.getToken();
        String pendingKey = RESET_TOKEN_PENDING_PREFIX + req.getToken();

        try {
            redisTemplate.rename(tokenKey, pendingKey);
        } catch (Exception e) {
            throw new AppException("Token khong hop le hoac da het han");
        }

        String email;
        try {
            email = redisTemplate.opsForValue().get(pendingKey);
            if (email == null) {
                throw new AppException("Token khong hop le hoac da het han");
            }

            AccountEntity account = accountRepository.findByEmailWithRole(email)
                    .orElseThrow(() -> new AppException("Tai khoan khong ton tai"));

            account.setPassword(passwordEncoder.encode(req.getNewPassword()));
            resetFailedLoginCounters(account);

            if ("Lock".equalsIgnoreCase(account.getStatus())
                    && LOCK_REASON_TOO_MANY_FAILED.equals(account.getLockReason())) {
                String previousStatus = account.getStatusBeforeLock();
                account.setStatus(previousStatus != null ? previousStatus : "Approved");
                account.setLockReason(null);
                account.setStatusBeforeLock(null);
            }

            accountRepository.save(account);
            completePasswordResetAfterSave(account, tokenKey, pendingKey);

            return ApiResponse.success("Dat lai mat khau thanh cong", null);

        } catch (Exception e) {
            restoreResetToken(tokenKey, pendingKey);
            throw e;
        }
    }

    private void completePasswordResetAfterSave(AccountEntity account, String tokenKey, String pendingKey) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deletePendingResetToken(pendingKey);
            sendPasswordChangedEmailBestEffort(account);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deletePendingResetToken(pendingKey);
                sendPasswordChangedEmailBestEffort(account);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    restoreResetToken(tokenKey, pendingKey);
                }
            }
        });
    }

    private void deletePendingResetToken(String pendingKey) {
        try {
            redisTemplate.delete(pendingKey);
        } catch (Exception e) {
            log.error("Failed to delete consumed reset token {}: {}", pendingKey, e.getMessage());
        }
    }

    private void restoreResetToken(String tokenKey, String pendingKey) {
        try {
            redisTemplate.rename(pendingKey, tokenKey);
        } catch (Exception restoreEx) {
            log.error("Failed to restore reset token {} after reset failure: {}",
                    tokenKey, restoreEx.getMessage());
        }
    }

    private void sendPasswordChangedEmailBestEffort(AccountEntity account) {
        try {
            securityEmailService.sendPasswordChangedEmail(account);
        } catch (Exception e) {
            log.warn("Failed to send password-changed email for account {}: {}",
                    account.getEmail(), e.getMessage());
        }
    }

    private String generateResetToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
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
            if (LOCK_REASON_TOO_MANY_FAILED.equals(account.getLockReason())) {
                throw new UnauthorizedException("Tai khoan da bi khoa, vui long dat lai mat khau");
            }
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
