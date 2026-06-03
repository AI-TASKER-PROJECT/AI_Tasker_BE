package com.aitasker.be.repository;
import com.aitasker.be.entity.AcceptanceCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AcceptanceCriteriaRepository extends JpaRepository<AcceptanceCriteriaEntity, Integer> { List<AcceptanceCriteriaEntity> findByMilestoneId(Integer milestoneId); }
