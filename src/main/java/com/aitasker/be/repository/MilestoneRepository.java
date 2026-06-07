package com.aitasker.be.repository;
import com.aitasker.be.entity.MilestoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MilestoneRepository extends JpaRepository<MilestoneEntity, Integer> {
    List<MilestoneEntity> findByContractIdOrderByOrderIndexAsc(Integer contractId);
    boolean existsByContractIdAndOrderIndex(Integer contractId, Integer orderIndex);
    List<MilestoneEntity> findByJobIdOrderByOrderIndexAsc(Integer jobId);
    boolean existsByJobIdAndOrderIndex(Integer jobId, Integer orderIndex);
}
