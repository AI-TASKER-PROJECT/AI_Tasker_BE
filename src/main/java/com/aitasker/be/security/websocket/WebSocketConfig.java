/*
 * NOTE FILE: src/main/java/com/aitasker/be/security/websocket/WebSocketConfig.java
 * Đây là file gì: Cấu hình WebSocket/STOMP để client nhận thông báo realtime từ back-end.
 * Mục đích note: giải thích các hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.security.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Note: Annotation này bật cấu hình WebSocket message broker của Spring.
@Configuration
@EnableWebSocketMessageBroker
// Note: Annotation này giúp Lombok sinh constructor cho interceptor dependency.
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthChannelInterceptor webSocketAuthChannelInterceptor;

    // Note: Hàm `registerStompEndpoints` khai báo endpoint /ws để frontend kết nối WebSocket/STOMP.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    // Note: Hàm `configureMessageBroker` cấu hình kênh gửi riêng từng user và prefix message của ứng dụng.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    // Note: Hàm `configureClientInboundChannel` gắn interceptor để xác thực JWT khi client CONNECT.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthChannelInterceptor);
    }
}
