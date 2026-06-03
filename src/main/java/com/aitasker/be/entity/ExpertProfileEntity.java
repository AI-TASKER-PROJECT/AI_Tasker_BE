package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "expert_profiles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ExpertProfileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expert_id") private Integer expertId;
    @Column(name = "account_id", nullable = false, unique = true) private Integer accountId;
    @Column(name = "national_id", nullable = false, unique = true, length = 50) private String nationalId;
    @Column(name = "id_card_front_url", length = 255) private String idCardFrontUrl;
    @Column(name = "id_card_back_url", length = 255) private String idCardBackUrl;
    @Column(name = "kyc_status", nullable = false, length = 50) private String kycStatus;
    @Column(name = "approved_by") private Integer approvedBy;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
