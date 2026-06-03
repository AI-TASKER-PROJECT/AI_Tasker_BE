package com.aitasker.be.repository;
import com.aitasker.be.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Integer> {}
