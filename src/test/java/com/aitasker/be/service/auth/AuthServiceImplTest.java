package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.UnauthorizedException;
import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.ForgotPasswordRequest;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RefreshTokenRequest;
import com.aitasker.be.dto.auth.ResetPasswordRequest;
import com.aitasker.be.common.response.ApiResponse;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.security.jwt.JwtService;
import com.aitasker.be.service.core.PaymentWalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailSendException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private AccountRepository accountRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private EmailOtpService emailOtpService;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private SecurityEmailService securityEmailService;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks private AuthServiceImpl authService;

    private AccountEntity testAccount;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        RoleEntity testRole = new RoleEntity();
        testRole.setRoleId(1);
        testRole.setRoleName("BUSINESS");

        testAccount = AccountEntity.builder()
                .accountId(1)
                .email("test@mail.com")
                .password("$2a$10$encoded")
                .fullName("Test User")
                .role(testRole)
                .status("Approved")
                .emailVerified(true)
                .build();

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@mail.com");
        loginRequest.setPassword("password123");

        ReflectionTestUtils.setField(authService, "loginFailedAttemptThreshold", 5);
        ReflectionTestUtils.setField(authService, "loginTemporaryLockMinutes", 5);
        ReflectionTestUtils.setField(authService, "passwordResetTokenTtlMinutes", 15);
        ReflectionTestUtils.setField(authService, "passwordResetRateLimitSeconds", 60);
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:5173");

        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(accountRepository.findByEmailWithRole(anyString()))
                .thenReturn(Optional.of(testAccount));
        lenient().when(accountRepository.findByEmailWithRoleForUpdate(anyString()))
                .thenReturn(Optional.of(testAccount));
    }

    private void mockCorrectPassword() {
        lenient().when(passwordEncoder.matches(eq("password123"), anyString())).thenReturn(true);
    }

    private void mockWrongPassword() {
        lenient().when(passwordEncoder.matches(eq("password123"), anyString())).thenReturn(false);
    }

    @Test
    void wrongPassword_shouldIncrementFailedAttempts() {
        mockWrongPassword();

        for (int i = 1; i <= 3; i++) {
            try {
                authService.login(loginRequest);
                fail("Should throw");
            } catch (UnauthorizedException e) {
                assertEquals("Sai email hoac mat khau", e.getMessage());
            }
        }

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, atLeast(3)).save(captor.capture());
        AccountEntity lastSaved = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(3, lastSaved.getFailedLoginAttempts());
        assertNotNull(lastSaved.getLastFailedLoginAt());
    }

    @Test
    void fifthWrongPassword_shouldTriggerTemporaryLockout() {
        mockWrongPassword();

        for (int i = 1; i <= 5; i++) {
            try {
                authService.login(loginRequest);
            } catch (UnauthorizedException ignored) {}
        }

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, times(5)).save(captor.capture());
        AccountEntity lastSaved = captor.getValue();
        assertNotNull(lastSaved.getLockedUntil());
        assertTrue(lastSaved.getLockedUntil().isAfter(LocalDateTime.now()));
        assertEquals(1, lastSaved.getLockoutCount());
        verify(securityEmailService).sendTemporaryLockoutEmail(any(AccountEntity.class));
    }

    @Test
    void loginDuringTemporaryLockout_shouldBeBlocked() {
        testAccount.setLockedUntil(LocalDateTime.now().plusMinutes(3));

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(loginRequest));
        assertEquals("Tai khoan dang bi tam khoa, vui long thu lai sau", ex.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void correctPasswordAfterLockoutExpiry_shouldResetCounters() {
        testAccount.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        testAccount.setFailedLoginAttempts(3);
        testAccount.setLockoutCount(1);
        testAccount.setLastFailedLoginAt(LocalDateTime.now().minusMinutes(10));

        mockCorrectPassword();
        when(jwtService.generateAccessToken(anyString(), anyString(), anyInt())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(anyString(), anyInt())).thenReturn("refresh-token");

        AuthResponse response = authService.login(loginRequest);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(captor.capture());
        AccountEntity saved = captor.getValue();
        assertEquals(0, saved.getFailedLoginAttempts());
        assertEquals(0, saved.getLockoutCount());
        assertNull(saved.getLockedUntil());
        assertNull(saved.getLastFailedLoginAt());
        assertEquals(1, saved.getActiveTokenVersion());
        assertNotNull(response.getAccessToken());
    }

    @Test
    void secondWrongPasswordCycle_shouldSecurityLockAccount() {
        testAccount.setLockoutCount(1);
        testAccount.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        mockWrongPassword();

        for (int i = 1; i <= 5; i++) {
            try {
                authService.login(loginRequest);
            } catch (UnauthorizedException ignored) {}
        }

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, times(5)).save(captor.capture());
        AccountEntity lastSaved = captor.getValue();
        assertEquals("Lock", lastSaved.getStatus());
        assertEquals("TOO_MANY_FAILED_LOGIN_ATTEMPTS", lastSaved.getLockReason());
        assertEquals("Approved", lastSaved.getStatusBeforeLock());
        verify(securityEmailService).sendSecurityLockEmail(any(AccountEntity.class), anyString());
    }

    @Test
    void securityLockedAccount_cannotLogin() {
        testAccount.setStatus("Lock");
        testAccount.setLockReason("TOO_MANY_FAILED_LOGIN_ATTEMPTS");

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.login(loginRequest));
        assertEquals("Tai khoan da bi khoa, vui long dat lai mat khau", ex.getMessage());
    }

    @Test
    void refreshToken_withValidRefreshToken_shouldIssueNewAccessToken() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh-token");

        when(jwtService.extractUsername("refresh-token")).thenReturn("test@mail.com");
        when(jwtService.isTokenValid("refresh-token", "test@mail.com")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractTokenVersion("refresh-token")).thenReturn(0);
        when(jwtService.generateAccessToken("test@mail.com", "BUSINESS", 0)).thenReturn("new-access-token");

        AuthResponse response = authService.refreshToken(req);

        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("BUSINESS", response.getRole());
        assertEquals("Approved", response.getAccountStatus());
        verify(jwtService, never()).generateRefreshToken(anyString());
    }

    @Test
    void refreshToken_withStaleTokenVersion_shouldReject() {
        testAccount.setActiveTokenVersion(2);
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh-token");

        when(jwtService.extractUsername("refresh-token")).thenReturn("test@mail.com");
        when(jwtService.isTokenValid("refresh-token", "test@mail.com")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractTokenVersion("refresh-token")).thenReturn(1);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.refreshToken(req));

        assertEquals("Phien dang nhap da het hieu luc", ex.getMessage());
        verify(jwtService, never()).generateAccessToken(anyString(), anyString(), anyInt());
    }

    @Test
    void refreshToken_withAccessToken_shouldReject() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("access-token");

        when(jwtService.extractUsername("access-token")).thenReturn("test@mail.com");
        when(jwtService.isTokenValid("access-token", "test@mail.com")).thenReturn(true);
        when(jwtService.isRefreshToken("access-token")).thenReturn(false);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.refreshToken(req));
        assertEquals("Refresh token khong hop le", ex.getMessage());
        verify(jwtService, never()).generateAccessToken(anyString(), anyString(), anyInt());
    }

    @Test
    void refreshToken_withLockedAccount_shouldReject() {
        testAccount.setStatus("Lock");
        testAccount.setLockReason("TOO_MANY_FAILED_LOGIN_ATTEMPTS");
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("refresh-token");

        when(jwtService.extractUsername("refresh-token")).thenReturn("test@mail.com");
        when(jwtService.isTokenValid("refresh-token", "test@mail.com")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> authService.refreshToken(req));
        assertEquals("Tai khoan da bi khoa, vui long dat lai mat khau", ex.getMessage());
        verify(jwtService, never()).generateAccessToken(anyString(), anyString(), anyInt());
    }

    @Test
    void login_shouldUseLockedAccountLookup() {
        mockWrongPassword();
        lenient().when(accountRepository.findByEmailWithRole(anyString())).thenReturn(Optional.empty());

        try { authService.login(loginRequest); } catch (UnauthorizedException ignored) {}

        verify(accountRepository, atLeastOnce()).findByEmailWithRoleForUpdate("test@mail.com");
    }

    @Test
    void temporaryLockoutEmailFailure_shouldStillPersistLockState() {
        mockWrongPassword();
        doThrow(new MailSendException("SMTP failure"))
                .when(securityEmailService).sendTemporaryLockoutEmail(any());

        for (int i = 1; i <= 5; i++) {
            try { authService.login(loginRequest); } catch (UnauthorizedException ignored) {}
        }

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, times(5)).save(captor.capture());
        AccountEntity lastSaved = captor.getValue();
        assertNotNull(lastSaved.getLockedUntil());
        assertEquals(1, lastSaved.getLockoutCount());
    }

    @Test
    void securityLockEmailFailure_shouldStillPersistLockState() {
        testAccount.setLockoutCount(1);
        testAccount.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        mockWrongPassword();
        doThrow(new MailSendException("SMTP failure"))
                .when(securityEmailService).sendSecurityLockEmail(any(), anyString());

        for (int i = 1; i <= 5; i++) {
            try { authService.login(loginRequest); } catch (UnauthorizedException ignored) {}
        }

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository, times(5)).save(captor.capture());
        AccountEntity lastSaved = captor.getValue();
        assertEquals("Lock", lastSaved.getStatus());
        assertEquals("TOO_MANY_FAILED_LOGIN_ATTEMPTS", lastSaved.getLockReason());
    }

    @Test
    void forgotPassword_withExistingEmail_shouldReturnSuccess() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("test@mail.com");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        ApiResponse<Void> response = authService.forgotPassword(req);

        assertTrue(response.isSuccess());
        assertEquals("Neu email ton tai, huong dan dat lai mat khau se duoc gui", response.getMessage());
        verify(securityEmailService).sendResetPasswordEmail(any(AccountEntity.class), anyString());
    }

    @Test
    void forgotPassword_withNonExistingEmail_shouldStillReturnSuccess() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("unknown@mail.com");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(accountRepository.findByEmailWithRole("unknown@mail.com"))
                .thenReturn(Optional.empty());

        ApiResponse<Void> response = authService.forgotPassword(req);

        assertTrue(response.isSuccess());
        assertEquals("Neu email ton tai, huong dan dat lai mat khau se duoc gui", response.getMessage());
        verify(securityEmailService, never()).sendResetPasswordEmail(any(), anyString());
    }

    @Test
    void forgotPassword_shouldBeRateLimited() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("test@mail.com");

        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        ApiResponse<Void> response = authService.forgotPassword(req);

        assertTrue(response.isSuccess());
        verify(accountRepository, never()).findByEmailWithRole(anyString());
    }

    @Test
    void resetPassword_withExpiredToken_shouldReject() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("expired-token");
        req.setNewPassword("newPassword123");

        doThrow(new RuntimeException("ERR no such key"))
                .when(redisTemplate)
                .rename("PASSWORD_RESET:expired-token", "PASSWORD_RESET_PENDING:expired-token");

        assertThrows(AppException.class, () -> authService.resetPassword(req));
    }

    @Test
    void resetPassword_withValidToken_shouldSucceed() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token"))
                .thenReturn("test@mail.com");

        ApiResponse<Void> response = authService.resetPassword(req);

        assertTrue(response.isSuccess());
        assertEquals("Dat lai mat khau thanh cong", response.getMessage());
        verify(accountRepository).save(any(AccountEntity.class));
        verify(redisTemplate).delete("PASSWORD_RESET_PENDING:valid-token");
        verify(securityEmailService).sendPasswordChangedEmail(any(AccountEntity.class));
    }

    @Test
    void resetPassword_sameTokenCannotBeUsedTwice() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token"))
                .thenReturn("test@mail.com");

        ApiResponse<Void> first = authService.resetPassword(req);
        assertTrue(first.isSuccess());

        doThrow(new RuntimeException("ERR no such key"))
                .when(redisTemplate)
                .rename("PASSWORD_RESET:valid-token", "PASSWORD_RESET_PENDING:valid-token");

        assertThrows(AppException.class, () -> authService.resetPassword(req));
    }

    @Test
    void resetPassword_concurrentClaim_rejectsSecondCaller() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("concurrent-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:concurrent-token"))
                .thenReturn("test@mail.com");

        authService.resetPassword(req);

        doThrow(new RuntimeException("ERR no such key"))
                .when(redisTemplate)
                .rename("PASSWORD_RESET:concurrent-token", "PASSWORD_RESET_PENDING:concurrent-token");

        assertThrows(AppException.class, () -> authService.resetPassword(req));
    }

    @Test
    void resetPassword_saveFailure_shouldNotDeleteToken() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token"))
                .thenReturn("test@mail.com");
        doThrow(new RuntimeException("DB down")).when(accountRepository).save(any());

        assertThrows(RuntimeException.class, () -> authService.resetPassword(req));
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void resetPassword_dbFailure_restoresTokenToOriginalKey() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token"))
                .thenReturn("test@mail.com");
        doThrow(new RuntimeException("DB down")).when(accountRepository).save(any());

        assertThrows(RuntimeException.class, () -> authService.resetPassword(req));

        verify(redisTemplate).rename(
                "PASSWORD_RESET_PENDING:valid-token",
                "PASSWORD_RESET:valid-token"
        );
        verify(redisTemplate, never()).delete(anyString());
    }

    @Test
    void resetPassword_transactionRollbackAfterMethodReturn_restoresPendingToken() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token"))
                .thenReturn("test@mail.com");

        TransactionSynchronizationManager.initSynchronization();
        try {
            ApiResponse<Void> response = authService.resetPassword(req);

            assertTrue(response.isSuccess());
            verify(accountRepository).save(any(AccountEntity.class));
            verify(redisTemplate, never()).delete("PASSWORD_RESET_PENDING:valid-token");
            verify(securityEmailService, never()).sendPasswordChangedEmail(any());

            List<TransactionSynchronization> synchronizations =
                    TransactionSynchronizationManager.getSynchronizations();
            assertEquals(1, synchronizations.size());

            synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);

            verify(redisTemplate).rename(
                    "PASSWORD_RESET_PENDING:valid-token",
                    "PASSWORD_RESET:valid-token"
            );
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void resetPassword_emailFailure_shouldStillPersistPasswordChange() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token"))
                .thenReturn("test@mail.com");
        doThrow(new MailSendException("SMTP failure"))
                .when(securityEmailService).sendPasswordChangedEmail(any());

        ApiResponse<Void> response = authService.resetPassword(req);

        assertTrue(response.isSuccess());
        assertEquals("Dat lai mat khau thanh cong", response.getMessage());
        verify(accountRepository).save(any(AccountEntity.class));
        verify(redisTemplate).delete("PASSWORD_RESET_PENDING:valid-token");
    }

    @Test
    void resetPassword_shouldUnlockTooManyFailedAccount() {
        testAccount.setStatus("Lock");
        testAccount.setLockReason("TOO_MANY_FAILED_LOGIN_ATTEMPTS");
        testAccount.setStatusBeforeLock("Approved");

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token")).thenReturn("test@mail.com");
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newHash");

        ApiResponse<Void> response = authService.resetPassword(req);

        assertTrue(response.isSuccess());
        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(captor.capture());
        AccountEntity saved = captor.getValue();
        assertEquals("Approved", saved.getStatus());
        assertNull(saved.getLockReason());
        assertNull(saved.getStatusBeforeLock());
        assertEquals(0, saved.getFailedLoginAttempts());
    }

    @Test
    void resetPassword_shouldNotUnlockAdminLockedAccount() {
        testAccount.setStatus("Lock");
        testAccount.setLockReason("ADMIN_LOCKED");

        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setToken("valid-token");
        req.setNewPassword("newPassword123");

        when(valueOperations.get("PASSWORD_RESET_PENDING:valid-token")).thenReturn("test@mail.com");

        authService.resetPassword(req);

        ArgumentCaptor<AccountEntity> captor = ArgumentCaptor.forClass(AccountEntity.class);
        verify(accountRepository).save(captor.capture());
        AccountEntity saved = captor.getValue();
        assertEquals("Lock", saved.getStatus());
        assertEquals("ADMIN_LOCKED", saved.getLockReason());
    }

    @Test
    void forgotPassword_emailFailure_shouldStillStoreToken() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("test@mail.com");

        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        doThrow(new MailSendException("SMTP failure"))
                .when(securityEmailService).sendResetPasswordEmail(any(), anyString());

        ApiResponse<Void> response = authService.forgotPassword(req);

        assertTrue(response.isSuccess());
        verify(valueOperations).set(
                startsWith("PASSWORD_RESET:"),
                eq("test@mail.com"),
                anyLong(),
                any()
        );
    }
}
