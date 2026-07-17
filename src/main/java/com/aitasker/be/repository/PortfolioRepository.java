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

    /**
     * Release A keeps the legacy text columns, but treats them strictly as
     * delimited numeric catalog IDs. The query is set-based and never performs
     * substring matching, so skill 1 cannot accidentally match skill 10/21.
     */
    @Query(value = """
            SELECT p.*
            FROM portfolios p
            JOIN expert_profiles ep ON ep.expert_id = p.expert_id
            JOIN account a ON a.account_id = ep.account_id
            JOIN roles r ON r.role_id = a.role_id
            WHERE LOWER(ep.kyc_status) = 'approved'
              AND LOWER(a.status) = 'approved'
              AND UPPER(r.role_name) = 'EXPERT'
              AND (
                    EXISTS (
                        SELECT 1
                        FROM regexp_split_to_table(COALESCE(p.skill_ids, ''), '[^0-9]+') token
                        WHERE token ~ '^[0-9]{1,9}$'
                          AND token::INTEGER = ANY(string_to_array(NULLIF(:skillIdsCsv, ''), ',')::INTEGER[])
                    )
                 OR EXISTS (
                        SELECT 1
                        FROM regexp_split_to_table(COALESCE(p.domain_ids, ''), '[^0-9]+') token
                        WHERE token ~ '^[0-9]{1,9}$'
                          AND token::INTEGER = ANY(string_to_array(NULLIF(:domainIdsCsv, ''), ',')::INTEGER[])
                    )
                 OR EXISTS (
                        SELECT 1
                        FROM regexp_split_to_table(COALESCE(p.technology_ids, ''), '[^0-9]+') token
                        WHERE token ~ '^[0-9]{1,9}$'
                          AND token::INTEGER = ANY(string_to_array(NULLIF(:technologyIdsCsv, ''), ',')::INTEGER[])
                    )
              )
            ORDER BY p.portfolio_id
            LIMIT :candidateLimit
            """, nativeQuery = true)
    List<PortfolioEntity> findEligibleCandidatesByCatalogIds(
            @Param("skillIdsCsv") String skillIdsCsv,
            @Param("domainIdsCsv") String domainIdsCsv,
            @Param("technologyIdsCsv") String technologyIdsCsv,
            @Param("candidateLimit") int candidateLimit
    );
}
