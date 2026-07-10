/*
 * NOTE FILE: src/main/java/com/aitasker/be/entity/KnowledgeChunkEntity.java
 * Day la file gi: File entity anh xa du lieu nghiep vu voi bang trong database de JPA doc/ghi.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Note: Annotation nay anh xa class Java voi mot bang trong database.
@Entity
// Note: Annotation nay chi ro bang database tuong ung voi entity.
@Table(name = "knowledge_chunks")
// Note: Annotation nay giup Lombok sinh getter de doc field.
@Getter
// Note: Annotation nay giup Lombok sinh setter de cap nhat field.
@Setter
// Note: Annotation nay giup Lombok tao builder de khoi tao object ro rang hon.
@Builder
// Note: Annotation nay giup Lombok sinh constructor rong cho JPA hoac deserialize du lieu.
@NoArgsConstructor
// Note: Annotation nay giup Lombok sinh constructor nhan day du field.
@AllArgsConstructor
public class KnowledgeChunkEntity {
    // Note: Annotation nay danh dau khoa chinh cua entity.
    @Id
    // Note: Annotation nay cau hinh cach database/JPA sinh gia tri khoa chinh.
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "source_file", nullable = false, length = 255)
    private String sourceFile;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "section_title", length = 255)
    private String sectionTitle;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "content", nullable = false)
    private String content;

    // Note: Annotation nay danh dau field chi dung trong Java, khong persist xuong database.
    @Transient
    private String embedding;

    // Note: Annotation nay cau hinh cot database tuong ung voi field entity.
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
