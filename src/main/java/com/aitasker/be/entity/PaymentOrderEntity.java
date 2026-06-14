package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "business_id")
    private Long businessId;

    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "job_id")
    private Long jobId;

    @Column(name = "milestone_id")
    private Long milestoneId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private PaymentProvider provider;

    @Column(name = "purpose", nullable = false, length = 50)
    private String purpose;

    @Column(name = "vnp_txn_ref", nullable = false, unique = true, length = 100)
    private String vnpTxnRef;

    @Column(name = "vnp_transaction_no", length = 100)
    private String vnpTransactionNo;

    @Column(name = "vnp_response_code", length = 20)
    private String vnpResponseCode;

    @Column(name = "vnp_secure_hash", columnDefinition = "TEXT")
    private String vnpSecureHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
