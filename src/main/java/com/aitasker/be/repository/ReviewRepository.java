/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/ReviewRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;

import com.aitasker.be.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Integer> {
    // Note: Hàm `findByContractId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<ReviewEntity> findByContractId(Integer contractId);
    boolean existsByContractIdAndReviewerId(Integer contractId, Integer reviewerId);

    @Query(value = "SELECT AVG(rating) FROM reviews WHERE reviewee_id = :revieweeId", nativeQuery = true)
    BigDecimal averageRatingByRevieweeId(@Param("revieweeId") Integer revieweeId);

    @Query(value = """
            SELECT ep.expert_id AS expertId, AVG(r.rating) AS averageRating
            FROM expert_profiles ep
            JOIN reviews r ON r.reviewee_id = ep.account_id
            WHERE ep.expert_id IN (:expertIds)
            GROUP BY ep.expert_id
            """, nativeQuery = true)
    List<ExpertRatingProjection> findAverageRatingsByExpertIds(@Param("expertIds") Collection<Integer> expertIds);

    interface ExpertRatingProjection {
        Integer getExpertId();
        BigDecimal getAverageRating();
    }
}
