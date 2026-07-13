package com.aitasker.be.repository;

import com.aitasker.be.entity.DisputeSelfResolveReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DisputeSelfResolveReplyRepository extends JpaRepository<DisputeSelfResolveReplyEntity, Long> {
    List<DisputeSelfResolveReplyEntity> findByDisputeIdOrderByCreatedAtAscReplyIdAsc(Integer disputeId);
    Optional<DisputeSelfResolveReplyEntity> findByReplyIdAndDisputeId(Long replyId, Integer disputeId);
    boolean existsByDisputeId(Integer disputeId);
}
