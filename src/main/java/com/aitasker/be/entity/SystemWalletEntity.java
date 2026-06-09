/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/SystemWalletEntity.java
 * Đây là file gì: File entity ánh xạ bảng database sang object Java, thể hiện cấu trúc dữ liệu được lưu trữ.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Note: Annotation này ánh xạ class Java với một bảng trong database.
@Entity
// Note: Annotation này chỉ rõ bảng database tương ứng với entity.
@Table(name = "system_wallet")
// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SystemWalletEntity {
    // Note: Annotation này đánh dấu khóa chính của entity.
    @Id
    // Note: Annotation này cấu hình cách database/JPA sinh giá trị khóa chính.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "system_wallet_id")
    private Long systemWalletId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    // Note: Annotation này mô tả quan hệ nhiều-bản-ghi tới một entity khác.
    @ManyToOne(fetch = FetchType.LAZY)
    // Note: Annotation này chỉ rõ cột khóa ngoại dùng để nối entity.
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @JsonIgnore
    private AccountEntity account;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "wallet_type", nullable = false, length = 30)
    private String walletType;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "transaction_id")
    private Long transactionId;

    // Note: Annotation này mô tả quan hệ nhiều-bản-ghi tới một entity khác.
    @ManyToOne(fetch = FetchType.LAZY)
    // Note: Annotation này chỉ rõ cột khóa ngoại dùng để nối entity.
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @JsonIgnore
    private TransactionEntity transaction;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "deposited_business_count", nullable = false)
    private Integer depositedBusinessCount;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "successful_deposit_count", nullable = false)
    private Integer successfulDepositCount;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "escrow_balance", nullable = false)
    private BigDecimal escrowBalance;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "holding_balance", nullable = false)
    private BigDecimal holdingBalance;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "disputed_balance", nullable = false)
    private BigDecimal disputedBalance;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    // Note: Annotation này cấu hình cột database tương ứng với field entity.
    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

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
