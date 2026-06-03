package com.aitasker.be.repository;

import com.aitasker.be.entity.SystemSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSettingEntity, String> {
}
