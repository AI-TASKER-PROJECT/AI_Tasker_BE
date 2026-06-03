package com.aitasker.be.repository;
import com.aitasker.be.entity.DisputeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DisputeRepository extends JpaRepository<DisputeEntity, Integer> { List<DisputeEntity> findByContractId(Integer contractId); }
