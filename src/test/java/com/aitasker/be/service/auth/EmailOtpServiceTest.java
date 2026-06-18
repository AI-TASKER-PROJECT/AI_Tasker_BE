package com.aitasker.be.service.auth;

import com.aitasker.be.common.exception.ResourceConflictException;
import com.aitasker.be.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailOtpServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private JavaMailSender mailSender;
    @Mock private AccountRepository accountRepository;

    @InjectMocks private EmailOtpService emailOtpService;

    @Test
    void sendOtp_shouldThrowWhenEmailAlreadyExists() {
        when(accountRepository.existsByEmailIgnoreCase("user@mail.com")).thenReturn(true);

        ResourceConflictException ex = assertThrows(ResourceConflictException.class,
                () -> emailOtpService.sendOtp(" User@Mail.Com "));

        assertEquals("Email đã tồn tại", ex.getMessage());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
