package com.aitasker.be.repository;

import com.aitasker.be.entity.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<StaffEntity, Integer> {
    Optional<StaffEntity> findByAccountId(Integer accountId);
}
