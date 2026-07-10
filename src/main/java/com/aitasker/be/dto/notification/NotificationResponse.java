/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/notification/NotificationResponse.java
 * Đây là file gì: DTO chuẩn hóa dữ liệu thông báo trả về cho REST API và WebSocket.
 * Mục đích note: giải thích annotation và dữ liệu response để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.notification;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Note: Annotation này giúp Lombok sinh getter, setter và builder cho dữ liệu response.
@Data
@Builder
public class NotificationResponse {
    private Integer notificationId;
    private String type;
    private String title;
    private String message;
    private String targetUrl;
    private Object metadata;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
