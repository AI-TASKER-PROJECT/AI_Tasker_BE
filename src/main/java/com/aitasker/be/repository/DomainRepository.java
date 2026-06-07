package com.aitasker.be.repository;

import com.aitasker.be.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DomainRepository extends JpaRepository<DomainEntity, Integer> {
    Optional<DomainEntity> findByDomainCode(String domainCode);
    boolean existsByDomainCode(String domainCode);
    List<DomainEntity> findByIsActiveTrueOrderBySortOrderAscDomainNameAsc();
}
