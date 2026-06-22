/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/NotificationEntity.java
 * Đây là file gì: Entity ánh xạ bảng notifications, lưu thông báo gửi cho từng tài khoản trong hệ thống.
 * Mục đích note: giải thích annotation và các hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class với bảng notifications để JPA lưu và đọc thông báo.
@Entity
@Table(name = "notifications")
// Note: Annotation này giúp Lombok sinh getter, setter, constructor và builder cho entity.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationEntity {
    // Note: Annotation này khai báo khóa chính tự tăng của thông báo.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id") private Integer notificationId;

    @Column(name = "receiver_account_id", nullable = false) private Integer receiverAccountId;
    @Column(name = "actor_account_id") private Integer actorAccountId;
    @Column(name = "type", nullable = false, length = 100) private String type;
    @Column(name = "title", nullable = false, length = 255) private String title;
    @Column(name = "message", nullable = false, columnDefinition = "TEXT") private String message;
    @Column(name = "target_url", length = 500) private String targetUrl;
    @Column(name = "metadata", columnDefinition = "TEXT") private String metadata;
    @Column(name = "is_read", nullable = false) private Boolean isRead;

    // Note: Annotation này tự ghi thời điểm tạo thông báo khi insert database.
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;

    @Column(name = "read_at") private LocalDateTime readAt;
}
