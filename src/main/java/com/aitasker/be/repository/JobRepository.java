package com.aitasker.be.repository;

import com.aitasker.be.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Integer> {
    List<JobEntity> findByBusinessId(Integer businessId);
}
