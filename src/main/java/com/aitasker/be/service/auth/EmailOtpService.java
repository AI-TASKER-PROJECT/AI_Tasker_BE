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

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final AccountRepository accountRepository;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static final String OTP_PREFIX = "EMAIL_OTP:";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED:";
    private static final long OTP_TTL_MINUTES = 1;
    private static final long VERIFIED_TTL_MINUTES = 30;

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

    public boolean isEmailVerified(String email) {
        String verified = redisTemplate.opsForValue().get(verifiedKey(normalizeEmail(email)));
        return "true".equals(verified);
    }

    public void clearVerifiedEmail(String email) {
        redisTemplate.delete(verifiedKey(normalizeEmail(email)));
    }

    private String otpKey(String email) {
        return OTP_PREFIX + email;
    }

    private String verifiedKey(String email) {
        return VERIFIED_PREFIX + email;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
