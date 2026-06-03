package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "contract_change_requests")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractChangeRequestEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id") private Integer requestId;
    @Column(name = "contract_id", nullable = false) private Integer contractId;
    @Column(name = "requested_by_account_id", nullable = false) private Integer requestedByAccountId;
    @Column(name = "change_type", nullable = false, length = 50) private String changeType;
    @Column(name = "change_summary", nullable = false) private String changeSummary;
    @Column(name = "proposed_budget") private BigDecimal proposedBudget;
    @Column(name = "proposed_timeline_days") private Integer proposedTimelineDays;
    @Column(name = "status", nullable = false, length = 20) private String status;
    @Column(name = "reviewed_by_account_id") private Integer reviewedByAccountId;
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
