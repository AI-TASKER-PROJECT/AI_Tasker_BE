/*
 * NOTE FILE: src/main/java/com/aitasker/be/security/websocket/WebSocketAuthChannelInterceptor.java
 * Đây là file gì: Interceptor xác thực JWT khi client CONNECT vào WebSocket/STOMP.
 * Mục đích note: giải thích các hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.security.websocket;

import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.repository.AccountRepository;
import com.aitasker.be.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

// Note: Annotation này cho Spring quản lý interceptor xác thực WebSocket.
@Component
// Note: Annotation này giúp Lombok sinh constructor cho các dependency final.
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {
    private final JwtService jwtService;
    private final AccountRepository accountRepository;

    // Note: Hàm `preSend` kiểm tra JWT ở frame CONNECT và gắn user principal theo accountId để gửi thông báo cá nhân.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = resolveToken(accessor);
            if (!StringUtils.hasText(token)) {
                throw new ForbiddenException("TOKEN WEBSOCKET KHONG HOP LE");
            }
            String email = jwtService.extractUsername(token);
            if (!jwtService.isTokenValid(token, email)) {
                throw new ForbiddenException("TOKEN WEBSOCKET DA HET HAN HOAC KHONG HOP LE");
            }
            AccountEntity account = accountRepository.findByEmailWithRole(email)
                    .orElseThrow(() -> new ForbiddenException("TAI KHOAN WEBSOCKET KHONG TON TAI"));
            Integer tokenVersion = jwtService.extractTokenVersion(token);
            if (tokenVersion == null || tokenVersion != account.getActiveTokenVersion()) {
                throw new ForbiddenException("PHIEN DANG NHAP WEBSOCKET DA HET HIEU LUC");
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    String.valueOf(account.getAccountId()),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().getRoleName()))
            );
            accessor.setUser(authentication);
        }
        return message;
    }

    // Note: Hàm `resolveToken` đọc token từ header Authorization hoặc token trong STOMP CONNECT frame.
    private String resolveToken(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (!StringUtils.hasText(authHeader)) {
            authHeader = accessor.getFirstNativeHeader("authorization");
        }
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        String token = accessor.getFirstNativeHeader("token");
        return StringUtils.hasText(token) ? token : authHeader;
    }
}
