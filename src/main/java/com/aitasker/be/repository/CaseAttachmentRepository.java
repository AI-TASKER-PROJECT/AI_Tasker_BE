package com.aitasker.be.repository;

import com.aitasker.be.entity.CaseAttachmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CaseAttachmentRepository extends JpaRepository<CaseAttachmentEntity, Long> {
    List<CaseAttachmentEntity> findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc(String ownerType, Long ownerId);
}
