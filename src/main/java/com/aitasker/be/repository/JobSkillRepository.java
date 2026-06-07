package com.aitasker.be.repository;

import com.aitasker.be.entity.JobSkillEntity;
import com.aitasker.be.entity.JobSkillId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobSkillRepository extends JpaRepository<JobSkillEntity, JobSkillId> {
    List<JobSkillEntity> findByIdJobId(Integer jobId);
    void deleteByIdJobId(Integer jobId);
}
