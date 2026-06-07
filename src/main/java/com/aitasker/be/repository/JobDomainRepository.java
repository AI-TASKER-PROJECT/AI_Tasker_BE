package com.aitasker.be.repository;

import com.aitasker.be.entity.JobDomainEntity;
import com.aitasker.be.entity.JobDomainId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobDomainRepository extends JpaRepository<JobDomainEntity, JobDomainId> {
    List<JobDomainEntity> findByIdJobId(Integer jobId);
    void deleteByIdJobId(Integer jobId);
}
