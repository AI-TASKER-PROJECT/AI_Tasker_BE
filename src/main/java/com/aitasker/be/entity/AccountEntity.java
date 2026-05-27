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

@Entity
@Table(name = "account")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class AccountEntity {
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

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) // optional = false: NGHĨA LÀ Ở MỖI BẢNG TABLE BẮT BUỘC PHẢI CÓ MỘT role_id 
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @CreationTimestamp // ANNOTATION CỦA HIBERNATE DÙNG ĐỂ GÁN TỰ ĐỘNG THỜI GIAN LÚC CREATE CHO FIELD
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // ANNOTATION CỦA HIBERNATE DÙNG ĐỂ GÁN TỰ ĐỘNG THỜI GIAN LÚC UPDATE CHO FIELD
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
