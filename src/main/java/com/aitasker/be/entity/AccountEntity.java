/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/AccountEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Note: Annotation này ánh xạ class Java với một bảng trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database tương ứng với entity.
@Table(name = "account")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@AllArgsConstructor @NoArgsConstructor
public class AccountEntity {
    // Note: Annotation này đánh dấu khóa chính của entity.
    @Id
    // Note: Annotation này cấu hình cách database/JPA sinh giá trị khóa chính.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "account_id")
    private Integer accountId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "phone", length = 20)
    private String phone;

<<<<<<< HEAD
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
=======
    @Column(name = "emailVerified", nullable = false)
    private boolean emailVerified;

>>>>>>> feat/week4-MST
    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    // Note: Annotation này mô tả quan hệ nhiều-bản-ghi tới một entity khác.
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional = false: NGHĨA LÀ Ở MỖI BẢNG TABLE BẮT BUỘC PHẢI CÓ MỘT role_id 
    // Note: Annotation này chỉ rõ cột khóa ngoại dùng để nối entity.
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    // Pending: chi duoc dang nhap va nop ho so. Approved: duoc dung day du chuc nang.
    // Rejected: duoc dang nhap de nop lai ho so. Lock: bi khoa dang nhap.
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @CreationTimestamp // ANNOTATION CỦA HIBERNATE DÙNG ĐỂ GÁN TỰ ĐỘNG THỜI GIAN LÚC CREATE CHO FIELD
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @UpdateTimestamp // ANNOTATION CỦA HIBERNATE DÙNG ĐỂ GÁN TỰ ĐỘNG THỜI GIAN LÚC UPDATE CHO FIELD
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
