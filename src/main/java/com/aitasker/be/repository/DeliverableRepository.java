/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/DeliverableRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;
import com.aitasker.be.entity.DeliverableEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DeliverableRepository extends JpaRepository<DeliverableEntity, Integer> { List<DeliverableEntity> findByMilestoneId(Integer milestoneId); }
