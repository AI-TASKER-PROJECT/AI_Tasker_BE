package com.aitasker.be.repository;

import com.aitasker.be.entity.ExpertProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExpertProfileRepository extends JpaRepository<ExpertProfileEntity, Integer> {
    Optional<ExpertProfileEntity> findByAccountId(Integer accountId);
    Optional<ExpertProfileEntity> findByNationalId(String nationalId);
}
