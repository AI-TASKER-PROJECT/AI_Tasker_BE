/*
 * NOTE FILE: src/main/java/com/aitasker/be/security/jwt/JwtService.java
 * Đây là file gì: File security cấu hình hoặc xử lý xác thực, phân quyền và JWT cho các API.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// Note: Annotation này cho Spring quản lý class như một service chứa nghiệp vụ.
@Service
public class JwtService {

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Value("${app.jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Value("${app.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    // Note: Hàm `generateAccessToken` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    public String generateAccessToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return generateToken(username, accessExpirationMs, claims);
    }

    // Note: Hàm `generateRefreshToken` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(username, refreshExpirationMs, claims);
    }

    // Note: Hàm `extractUsername` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Note: Hàm `isTokenValid` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    public boolean isTokenValid(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    // Note: Hàm `extractRole` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Note: Hàm `generateToken` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    private String generateToken(String username, long expirationMs, Map<String, Object> claims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }

    // Note: Hàm `isTokenExpired` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Note: Hàm `extractClaim` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    // Note: Hàm `getSigningKey` phục vụ xác thực/phân quyền, xử lý JWT hoặc lấy thông tin người dùng hiện tại.
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
