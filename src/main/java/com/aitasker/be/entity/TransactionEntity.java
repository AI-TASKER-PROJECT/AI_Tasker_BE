package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "transactions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TransactionEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id") private Long transactionId;
    @Column(name = "milestone_id", nullable = false) private Integer milestoneId;
    @Column(name = "amount", nullable = false) private BigDecimal amount;
    @Column(name = "commission_fee", nullable = false) private BigDecimal commissionFee;
    @Column(name = "transaction_type", nullable = false, length = 50) private String transactionType;
    @Column(name = "status", nullable = false, length = 50) private String status;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
