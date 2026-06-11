/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/SystemSettingEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với một bảng trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database tương ứng với entity.
@Table(name = "system_settings")
// Note: Annotation này giúp Lombok sinh getter để đọc field.
@Getter
// Note: Annotation này giúp Lombok sinh setter để cập nhật field.
@Setter
// Note: Annotation này giúp Lombok tạo builder để khởi tạo object rõ ràng hơn.
@Builder
// Note: Annotation này giúp Lombok sinh constructor rỗng cho JPA hoặc deserialize dữ liệu.
@NoArgsConstructor
// Note: Annotation này giúp Lombok sinh constructor nhận đầy đủ field.
@AllArgsConstructor
public class SystemSettingEntity {
    // Note: Annotation này đánh dấu khóa chính của entity.
    @Id
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "setting_key", length = 100)
    private String settingKey;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "setting_value", nullable = false)
    private String settingValue;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "description")
    private String description;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "updated_by_role_id")
    private Integer updatedByRoleId;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
