package com.aitasker.be.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class EmailOtpService {

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    private static final String OTP_PREFIX = "EMAIL_OTP:";
    private static final String VERIFIED_PREFIX = "EMAIL_VERIFIED:";
    private static final long OTP_TTL_MINUTES = 5;
    private static final long VERIFIED_TTL_MINUTES = 30;

    public void sendOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        String otp = String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 1000000)
        );

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
        message.setSubject("AI Tasker Email Verification OTP");
        message.setText("Ma OTP xac thuc email cua ban la: " + otp + ". Ma co hieu luc trong 5 phut.");

        mailSender.send(message);
    }

    public boolean verifyOtp(String email, String otp) {
        String normalizedEmail = normalizeEmail(email);
        String savedOtp = redisTemplate.opsForValue()
                .get(otpKey(normalizedEmail));

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
        String verified = redisTemplate.opsForValue()
                .get(verifiedKey(normalizeEmail(email)));

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
