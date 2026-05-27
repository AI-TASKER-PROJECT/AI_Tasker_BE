package com.aitasker.be.service.auth;

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

    @Override
@Transactional
public AuthResponse register(RegisterRequest req) {
    if (accountRepository.existsByEmail(req.getEmail())) {
        throw new ResourceConflictException("Email đã tồn tại");
    }

    RoleEntity role = roleRepository.findByRoleName(req.getRole())
            .orElseThrow(() -> new UnauthorizedException("Role không hợp lệ"));

    AccountEntity account = AccountEntity.builder()
            .email(req.getEmail())
            .password(passwordEncoder.encode(req.getPassword()))
            .phone(req.getPhone())
            .fullName(req.getFullName())
            .role(role)
            .isActive(true)
            .build();

    AccountEntity saved = accountRepository.save(account);

    String accessToken = jwtService.generateAccessToken(saved.getEmail());
    String refreshToken = jwtService.generateRefreshToken(saved.getEmail());

    return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .role(saved.getRole().getRoleName())
            .email(saved.getEmail())
            .fullName(saved.getFullName())
            .build();
}

@Override
@Transactional(readOnly = true)
public AuthResponse login(LoginRequest req) {
    AccountEntity account = accountRepository.findByEmailWithRole(req.getEmail())
            .orElseThrow(() -> new UnauthorizedException("Sai email hoặc mật khẩu"));

    if (!Boolean.TRUE.equals(account.getIsActive())) {
        throw new UnauthorizedException("Tài khoản đã bị khóa");
    }

    if (!passwordEncoder.matches(req.getPassword(), account.getPassword())) {
        throw new UnauthorizedException("Sai email hoặc mật khẩu");
    }

    String accessToken = jwtService.generateAccessToken(account.getEmail());
    String refreshToken = jwtService.generateRefreshToken(account.getEmail());

    return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .role(account.getRole().getRoleName())
            .email(account.getEmail())
            .fullName(account.getFullName())
            .build();
}
}