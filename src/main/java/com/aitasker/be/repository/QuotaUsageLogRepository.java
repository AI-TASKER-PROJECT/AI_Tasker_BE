/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/QuotaUsageLogRepository.java
 * Day la file gi: File repository dinh nghia cong truy cap du lieu, de Spring Data JPA sinh truy van toi database.
 * Muc dich note: giai thich cac annotation va ham chinh de doc hieu chuc nang code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.QuotaUsageLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotaUsageLogRepository extends JpaRepository<QuotaUsageLogEntity, Long> {
}
