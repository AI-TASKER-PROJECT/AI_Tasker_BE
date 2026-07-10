/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/RoleEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Note: Annotation này ánh xạ class Java với một bảng trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database tương ứng với entity.
@Table(name = "roles")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@AllArgsConstructor @NoArgsConstructor
public class RoleEntity {
    // Note: Annotation này đánh dấu khóa chính của entity.
    @Id
    // Note: Annotation này cấu hình cách database/JPA sinh giá trị khóa chính.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "role_id")
    private Integer roleId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String roleName;

    // Note: Annotation này mô tả quan hệ một-bản-ghi tới nhiều entity khác.
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @JsonIgnore
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Builder.Default // KHI BULDER MÀ KO TRUYỀN GIÁ TRỊ THÌ SẼ DÙNG GIÁ TRỊ MẶC ĐỊNH ĐÃ KHAI BÁO SẴN
    private List<AccountEntity> accounts = new ArrayList<>();
}
