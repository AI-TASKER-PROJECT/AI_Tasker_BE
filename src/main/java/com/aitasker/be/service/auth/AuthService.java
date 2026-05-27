package com.aitasker.be.service.auth;

import com.aitasker.be.dto.auth.AuthResponse;
import com.aitasker.be.dto.auth.LoginRequest;
import com.aitasker.be.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest req);
    AuthResponse login(LoginRequest req);
}