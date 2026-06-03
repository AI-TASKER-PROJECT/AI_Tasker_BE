package com.aitasker.be.repository;

import com.aitasker.be.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Integer> {
    Optional<PortfolioEntity> findByExpertId(Integer expertId);
}
