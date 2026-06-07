package com.aitasker.be.repository;
import com.aitasker.be.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ContractRepository extends JpaRepository<ContractEntity, Integer> {
    List<ContractEntity> findByBusinessId(Integer businessId);
    List<ContractEntity> findByExpertId(Integer expertId);
    Optional<ContractEntity> findByJobId(Integer jobId);
}
