package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_wallet")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SystemWalletEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "system_wallet_id")
    private Long systemWalletId;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", insertable = false, updatable = false)
    @JsonIgnore
    private AccountEntity account;

    @Column(name = "role_id", nullable = false)
    private Integer roleId;

    @Column(name = "wallet_type", nullable = false, length = 30)
    private String walletType;

    @Column(name = "transaction_id")
    private Long transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    @JsonIgnore
    private TransactionEntity transaction;

    @Column(name = "deposited_business_count", nullable = false)
    private Integer depositedBusinessCount;

    @Column(name = "successful_deposit_count", nullable = false)
    private Integer successfulDepositCount;

    @Column(name = "current_balance", nullable = false)
    private BigDecimal currentBalance;

    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance;

    @Column(name = "escrow_balance", nullable = false)
    private BigDecimal escrowBalance;

    @Column(name = "total_revenue", nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "holding_balance", nullable = false)
    private BigDecimal holdingBalance;

    @Column(name = "disputed_balance", nullable = false)
    private BigDecimal disputedBalance;

    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
