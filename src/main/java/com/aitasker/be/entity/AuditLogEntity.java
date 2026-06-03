package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity @Table(name = "audit_logs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuditLogEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id") private Integer logId;
    @Column(name = "actor_account_id", nullable = false) private Integer actorAccountId;
    @Column(name = "action", nullable = false, length = 100) private String action;
    @Column(name = "entity_name", nullable = false, length = 100) private String entityName;
    @Column(name = "entity_id", length = 100) private String entityId;
    // MAP KIEU JSONB DUNG CHUAN DE TRANH LOI EP KIEU VARCHAR -> JSONB KHI INSERT AUDIT LOG.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value_json", columnDefinition = "jsonb") private Object oldValueJson;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value_json", columnDefinition = "jsonb") private Object newValueJson;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @Column(name = "user_agent") private String userAgent;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
}
