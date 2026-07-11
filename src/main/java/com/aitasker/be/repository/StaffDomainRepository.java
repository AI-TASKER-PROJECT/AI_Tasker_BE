package com.aitasker.be.repository;

import com.aitasker.be.entity.StaffDomainEntity;
import com.aitasker.be.entity.StaffDomainId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffDomainRepository extends JpaRepository<StaffDomainEntity, StaffDomainId> {
    List<StaffDomainEntity> findByIdStaffId(Integer staffId);
    void deleteByIdStaffId(Integer staffId);
}
