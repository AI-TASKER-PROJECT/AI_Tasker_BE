/*
 * NOTE FILE: src/main/java/com/aitasker/be/dto/admin/AuditLogResponse.java
 * Đây là file gì: DTO trả dữ liệu audit log cho giao diện admin, kèm thông tin actor để phân nhóm nội bộ/bên ngoài.
 * Mục đích note: giải thích annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.dto.admin;

import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.AuditLogEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

// Note: Annotation này giúp Lombok sinh getter, setter và các hàm tiện ích cho dữ liệu.
@Data
// Note: Annotation này giúp Lombok tạo builder để khởi tạo object rõ ràng hơn.
@Builder
public class AuditLogResponse {
    private Integer logId;
    private Integer actorAccountId;
    private String actor;
    private String actorEmail;
    private String actorRole;
    private String actorGroup;
    private String action;
    private String entityName;
    private String entityId;
    private String rawEntityName;
    private String rawEntityId;
    private String entityDisplayName;
    private String entityOwner;
    private String entityOwnerEmail;
    private String entityOwnerRole;
    private Object oldValueJson;
    private Object newValueJson;
    private LocalDateTime createdAt;

    // Note: Hàm `from` chuyển entity audit log sang response có đủ thông tin người thực hiện và nhóm role để UI hiển thị.
    public static AuditLogResponse from(AuditLogEntity log, AccountEntity actor, String actorGroup, String displayAction) {
        return AuditLogResponse.builder()
                .logId(log.getLogId())
                .actorAccountId(log.getActorAccountId())
                .actor(actor == null ? "Không xác định" : actor.getFullName())
                .actorEmail(actor == null ? null : actor.getEmail())
                .actorRole(actor == null || actor.getRole() == null ? null : actor.getRole().getRoleName())
                .actorGroup(actorGroup)
                .action(displayAction)
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .rawEntityName(log.getEntityName())
                .rawEntityId(log.getEntityId())
                .oldValueJson(log.getOldValueJson())
                .newValueJson(log.getNewValueJson())
                .createdAt(log.getCreatedAt())
                .build();
    }

    // Note: Hàm `attachEntityInfo` gắn tên đối tượng và tài khoản sở hữu để admin đọc audit log dễ hiểu hơn mã kỹ thuật.
    public AuditLogResponse attachEntityInfo(String displayName, AccountEntity owner) {
        return attachEntityInfo(displayName, displayName, owner);
    }

    public AuditLogResponse attachEntityInfo(String displayName, String displayEntityName, AccountEntity owner) {
        this.entityDisplayName = displayName;
        this.entityName = displayEntityName;
        this.entityId = null;
        if (owner != null) {
            this.entityOwner = owner.getFullName();
            this.entityOwnerEmail = owner.getEmail();
            this.entityOwnerRole = owner.getRole() == null ? null : owner.getRole().getRoleName();
        }
        return this;
    }

    // Note: Hàm `fallbackEntityOwner` dùng người thực hiện làm thông tin đối tượng khi entity không tìm được owner riêng.
    public AuditLogResponse fallbackEntityOwner(AccountEntity fallbackOwner) {
        if (this.entityOwner == null && fallbackOwner != null) {
            this.entityOwner = fallbackOwner.getFullName();
            this.entityOwnerEmail = fallbackOwner.getEmail();
            this.entityOwnerRole = fallbackOwner.getRole() == null ? null : fallbackOwner.getRole().getRoleName();
        }
        return this;
    }
}
