/*
 * NOTE FILE: src/main/java/com/aitasker/be/security/filter/JwtAuthenticationFilter.java
 * Đây là file gì: File security cấu hình hoặc xử lý xác thực, phân quyền và JWT cho các API.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.security.filter;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.security.jwt.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Note: Annotation này cho Spring quản lý class như một component dùng chung.
@Component
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String[] authParts = authHeader.trim().split("\\s+", 2);
        if (authParts.length != 2 || !"bearer".equalsIgnoreCase(authParts[0])) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authParts[1];
        String username;
        boolean allowAnonymousAuthEndpoint = request.getRequestURI().startsWith("/api/auth/");
        try {
            username = jwtService.extractUsername(token);
        } catch (JwtException | IllegalArgumentException ex) {
            if (allowAnonymousAuthEndpoint) {
                filterChain.doFilter(request, response);
                return;
            }
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid or expired token\",\"data\":null}");
            return;
        }

        try {
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                AccountEntity account = accountRepository.findByEmailWithRole(username).orElse(null);
                if (account == null
                        || !jwtService.isTokenValid(token, username)
                        || !isCurrentTokenVersion(token, account)) {
                    if (allowAnonymousAuthEndpoint) {
                        filterChain.doFilter(request, response);
                        return;
                    }
                    SecurityContextHolder.clearContext();
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"success\":false,\"message\":\"Invalid or expired token\",\"data\":null}");
                    return;
                }

                    String role = account.getRole() == null ? null : account.getRole().getRoleName();
                    if (role != null && !role.isBlank()) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        username,
                                        null,
                                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                                );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            if (allowAnonymousAuthEndpoint) {
                filterChain.doFilter(request, response);
                return;
            }
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid or expired token\",\"data\":null}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isCurrentTokenVersion(String token, AccountEntity account) {
        Integer tokenVersion = jwtService.extractTokenVersion(token);
        return tokenVersion != null && tokenVersion == account.getActiveTokenVersion();
    }
}
