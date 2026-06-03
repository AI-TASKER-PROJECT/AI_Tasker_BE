package com.aitasker.be.repository;
import com.aitasker.be.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> { List<TransactionEntity> findByMilestoneId(Integer milestoneId); }
