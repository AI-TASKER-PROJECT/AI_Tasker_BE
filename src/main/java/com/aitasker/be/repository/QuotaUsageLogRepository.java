package com.aitasker.be.repository;

import com.aitasker.be.entity.QuotaUsageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotaUsageLogRepository extends JpaRepository<QuotaUsageLogEntity, Long> {
}
