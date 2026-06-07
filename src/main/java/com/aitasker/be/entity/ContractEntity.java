package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name = "contracts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ContractEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id") private Integer contractId;
    @Column(name = "job_id", nullable = false) private Integer jobId;
    @Column(name = "business_id", nullable = false) private Integer businessId;
    @Column(name = "expert_id", nullable = false) private Integer expertId;
    @Column(name = "technology_used", length = 255) private String technologyUsed;
    @Column(name = "total_budget", nullable = false) private BigDecimal totalBudget;
    @Column(name = "timeline_days", nullable = false) private Integer timelineDays;
    @Column(name = "nda_signed", nullable = false) private Boolean ndaSigned;
    @Column(name = "status", nullable = false, length = 50) private String status;
    @Column(name = "business_accepted_at") private LocalDateTime businessAcceptedAt;
    @Column(name = "expert_accepted_at") private LocalDateTime expertAcceptedAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
