package com.aitasker.be.repository;

import com.aitasker.be.entity.SkillEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkillRepository extends JpaRepository<SkillEntity, Integer> {
    Optional<SkillEntity> findBySkillCode(String skillCode);
    boolean existsBySkillCode(String skillCode);
    List<SkillEntity> findByIsActiveTrueOrderBySkillNameAsc();
}
