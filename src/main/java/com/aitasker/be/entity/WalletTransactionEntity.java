package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "system_wallet_id", nullable = false)
    private Long systemWalletId;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "payment_order_id")
    private Long paymentOrderId;

    @Column(name = "transaction_type", nullable = false, length = 50)
    private String transactionType;

    @Column(name = "direction", nullable = false, length = 10)
    private String direction;

    @Column(name = "balance_type", nullable = false, length = 20)
    private String balanceType;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
