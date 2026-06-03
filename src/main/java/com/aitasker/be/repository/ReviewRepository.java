package com.aitasker.be.repository;

import com.aitasker.be.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    List<ReviewEntity> findByContractId(Integer contractId);
    boolean existsByContractIdAndReviewerId(Integer contractId, Integer reviewerId);
}
