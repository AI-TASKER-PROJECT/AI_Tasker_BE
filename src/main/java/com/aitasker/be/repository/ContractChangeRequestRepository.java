package com.aitasker.be.repository;
import com.aitasker.be.entity.ContractChangeRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ContractChangeRequestRepository extends JpaRepository<ContractChangeRequestEntity, Integer> { List<ContractChangeRequestEntity> findByContractId(Integer contractId); }
