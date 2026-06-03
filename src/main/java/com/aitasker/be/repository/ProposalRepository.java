package com.aitasker.be.repository;

import com.aitasker.be.entity.ProposalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRepository extends JpaRepository<ProposalEntity, Integer> {
    boolean existsByJobIdAndExpertId(Integer jobId, Integer expertId);
    List<ProposalEntity> findByJobId(Integer jobId);
}
