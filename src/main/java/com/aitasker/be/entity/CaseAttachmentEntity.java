package com.aitasker.be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "case_attachments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CaseAttachmentEntity {
    public static final String OWNER_DISPUTE = "DISPUTE";
    public static final String OWNER_TERMINATION_REQUEST = "TERMINATION_REQUEST";
    public static final String OWNER_DELIVERABLE_REJECTION = "DELIVERABLE_REJECTION";
    public static final String OWNER_PARTIAL_EVIDENCE = "PARTIAL_EVIDENCE";
    public static final String OWNER_STAFF_REPORT = "STAFF_REPORT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attachment_id")
    private Long attachmentId;
    @Column(name = "owner_type", nullable = false, length = 50)
    private String ownerType;
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;
    @Column(name = "uploaded_by_account_id", nullable = false)
    private Integer uploadedByAccountId;
    @Column(name = "file_url", nullable = false)
    private String fileUrl;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_type", length = 100)
    private String fileType;
    @Column(name = "note")
    private String note;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
