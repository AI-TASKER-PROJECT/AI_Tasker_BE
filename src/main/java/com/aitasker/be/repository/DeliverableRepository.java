package com.aitasker.be.repository;
import com.aitasker.be.entity.DeliverableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DeliverableRepository extends JpaRepository<DeliverableEntity, Integer> { List<DeliverableEntity> findByMilestoneId(Integer milestoneId); }
