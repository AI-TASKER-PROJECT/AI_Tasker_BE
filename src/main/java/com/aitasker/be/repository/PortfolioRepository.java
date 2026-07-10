/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/PortfolioRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.PortfolioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<PortfolioEntity, Integer> {
    // Note: Hàm `findByExpertId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Optional<PortfolioEntity> findByExpertId(Integer expertId);

    // Note: Query rộng theo keyword/id trong chuỗi skill_ids/domain_ids; service sẽ lọc exact lại để tránh match nhầm.
    @Query("""
            SELECT p
            FROM PortfolioEntity p
            WHERE LOWER(COALESCE(p.skillIds, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(COALESCE(p.domainIds, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<PortfolioEntity> findCandidatesBySkillOrDomainKeyword(@Param("keyword") String keyword);
}
