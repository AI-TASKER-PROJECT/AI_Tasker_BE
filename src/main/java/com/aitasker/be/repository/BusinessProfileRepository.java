package com.aitasker.be.repository;

import com.aitasker.be.entity.BusinessProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfileEntity, Integer> {
    Optional<BusinessProfileEntity> findByAccountId(Integer accountId);
    boolean existsByTaxCode(String taxCode);
}
