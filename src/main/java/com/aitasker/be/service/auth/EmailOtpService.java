/*
 * NOTE FILE: src/main/java/com/aitasker/be/service/auth/EmailOtpService.java
 * Day la file gi: File service chua nghiep vu chinh, dieu phoi repository va kiem tra luat xu ly cua he thong.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.ResourceConflictException;
import com.aitasker.be.dto.auth.SendOtpResponse;
import com.aitasker.be.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

// Note: Annotation nay cho Spring quan ly class nhu mot service chua nghiep vu.
@Service
// Note: Annotation nay giup Lombok sinh constructor cho cac dependency final.
@RequiredArgsConstructor
public class EmailOtpService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final AccountRepository accountRepository;

    // Note: Annotation nay inject gia tri cau hinh vao field hoac tham so.
    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static final String OTP_PREFIX = "EMAIL_OTP:";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED:";
    private static final long OTP_TTL_MINUTES = 1;
    private static final long VERIFIED_TTL_MINUTES = 30;

    // Note: Ham `sendOtp` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public SendOtpResponse sendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (accountRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResourceConflictException("Email da ton tai");
        }

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        long expiresInSeconds = TimeUnit.MINUTES.toSeconds(OTP_TTL_MINUTES);
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expiresInSeconds);

        redisTemplate.opsForValue().set(
                otpKey(normalizedEmail),
                otp,
                OTP_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        SimpleMailMessage message = new SimpleMailMessage();
        if (mailFrom != null && !mailFrom.isBlank()) {
            message.setFrom(mailFrom);
        }
        message.setTo(normalizedEmail);
        message.setSubject("Xac minh bang ma OTP qua Email");
        message.setText("Ma OTP xac thuc cua ban la: " + otp);

        mailSender.send(message);

        return SendOtpResponse.builder()
                .expiresAt(expiresAt)
                .expiresInSeconds(expiresInSeconds)
                .build();
    }

    // Note: Ham `verifyOtp` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public boolean verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        String savedOtp = redisTemplate.opsForValue().get(otpKey(normalizedEmail));

        if (savedOtp == null) {
            return false;
        }

        if (!savedOtp.equals(otp == null ? null : otp.trim())) {
            return false;
        }

        redisTemplate.delete(otpKey(normalizedEmail));

        redisTemplate.opsForValue().set(
                verifiedKey(normalizedEmail),
                "true",
                VERIFIED_TTL_MINUTES,
                TimeUnit.MINUTES
        );

        return true;
    }

    // Note: Ham `isEmailVerified` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public boolean isEmailVerified(String email) {
        String verified = redisTemplate.opsForValue().get(verifiedKey(normalizeEmail(email)));
        return "true".equals(verified);
    }

    // Note: Ham `clearVerifiedEmail` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    public void clearVerifiedEmail(String email) {
        redisTemplate.delete(verifiedKey(normalizeEmail(email)));
    }

    // Note: Ham `otpKey` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String otpKey(String email) {
        return OTP_PREFIX + email;
    }

    // Note: Ham `verifiedKey` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String verifiedKey(String email) {
        return VERIFIED_PREFIX + email;
    }

    // Note: Ham `normalizeEmail` xu ly nghiep vu chinh, kiem tra dieu kien va phoi hop repository/service lien quan.
    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
