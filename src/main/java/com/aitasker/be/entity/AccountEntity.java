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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.

    @Column(name = "emailVerified", nullable = false)
    private boolean emailVerified;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional = false: NGHĨA LÀ Ở MỖI BẢNG TABLE BẮT BUỘC PHẢI CÓ MỘT role_id 
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    // Pending: chi duoc dang nhap va nop ho so. Approved: duoc dung day du chuc nang.
    // Rejected: duoc dang nhap de nop lai ho so. Lock: bi khoa dang nhap.
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @CreationTimestamp // ANNOTATION CỦA HIBERNATE DÙNG ĐỂ GÁN TỰ ĐỘNG THỜI GIAN LÚC CREATE CHO FIELD
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // ANNOTATION CỦA HIBERNATE DÙNG ĐỂ GÁN TỰ ĐỘNG THỜI GIAN LÚC UPDATE CHO FIELD
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
