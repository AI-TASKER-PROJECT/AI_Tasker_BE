/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/AuditLogEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Entity @Table(name = "audit_logs")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLogEntity {
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "log_id") private Integer logId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "actor_account_id", nullable = false) private Integer actorAccountId;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "action", nullable = false, length = 100) private String action;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "entity_name", nullable = false, length = 100) private String entityName;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "entity_id", length = 100) private String entityId;
    // MAP KIEU JSONB DUNG CHUAN DE TRANH LOI EP KIEU VARCHAR -> JSONB KHI INSERT AUDIT LOG.
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @JdbcTypeCode(SqlTypes.JSON)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "old_value_json", columnDefinition = "jsonb") private Object oldValueJson;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @JdbcTypeCode(SqlTypes.JSON)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "new_value_json", columnDefinition = "jsonb") private Object newValueJson;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "ip_address", length = 45) private String ipAddress;
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "user_agent") private String userAgent;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
