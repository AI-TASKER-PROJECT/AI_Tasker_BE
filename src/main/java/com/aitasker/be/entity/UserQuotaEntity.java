package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_quotas")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserQuotaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quota_id")
    private Long quotaId;

    @Column(name = "account_id", nullable = false, unique = true)
    private Integer accountId;

    @Column(name = "job_post_quota_balance", nullable = false)
    private Integer jobPostQuotaBalance;

    @Column(name = "proposal_quota_balance", nullable = false)
    private Integer proposalQuotaBalance;

    @Column(name = "badge_expired_at")
    private LocalDateTime badgeExpiredAt;

    @Column(name = "premium_expired_at")
    private LocalDateTime premiumExpiredAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
