package com.aitasker.be.repository;

import com.aitasker.be.entity.StaffSkillEntity;
import com.aitasker.be.entity.StaffSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffSkillRepository extends JpaRepository<StaffSkillEntity, StaffSkillId> {
    List<StaffSkillEntity> findByIdStaffId(Integer staffId);
    void deleteByIdStaffId(Integer staffId);
}
