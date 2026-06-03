package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity @Table(name = "business_profiles")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BusinessProfileEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "business_id") private Integer businessId;
    @Column(name = "account_id", nullable = false, unique = true) private Integer accountId;
    @Column(name = "tax_code", nullable = false, unique = true, length = 50) private String taxCode;
    @Column(name = "company_name", nullable = false, length = 255) private String companyName;
    @Column(name = "address") private String address;
    @Column(name = "business_license_url", length = 255) private String businessLicenseUrl;
    @Column(name = "kyb_status", nullable = false, length = 50) private String kybStatus;
    @Column(name = "approved_by") private Integer approvedBy;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;
}
