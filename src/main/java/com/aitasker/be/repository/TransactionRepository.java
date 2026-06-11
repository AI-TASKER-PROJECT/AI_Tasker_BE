/*
 * NOTE FILE: src/main/java/com/aitasker/be/repository/TransactionRepository.java
 * Đây là file gì: File repository định nghĩa cổng truy cập dữ liệu, để Spring Data JPA sinh truy vấn tới database.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.repository;
import com.aitasker.be.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    // Note: Hàm `findByMilestoneId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    List<TransactionEntity> findByMilestoneId(Integer milestoneId);

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Query("""
            select coalesce(sum(t.commissionFee), 0)
            from TransactionEntity t
            where t.status = 'Success'
            """)
    // Note: Hàm `sumSuccessfulCommissionFee` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    BigDecimal sumSuccessfulCommissionFee();

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Query("""
            select coalesce(sum(
                case
                    when t.transactionType = 'Deposit' then t.amount
                    when t.transactionType in ('Payout', 'Refund') then -t.amount
                    else 0
                end
            ), 0)
            from TransactionEntity t
            where t.status = 'Success'
            """)
    // Note: Hàm `calculateHoldingBalance` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    BigDecimal calculateHoldingBalance();

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Query("""
            select count(distinct c.businessId)
            from TransactionEntity t
            join MilestoneEntity m on m.milestoneId = t.milestoneId
            join ContractEntity c on c.jobId = m.jobId
            where t.status = 'Success' and t.transactionType = 'Deposit'
            """)
    long countDepositedBusinesses();

    long countByStatusAndTransactionType(String status, String transactionType);

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Query("select coalesce(max(t.transactionId), 0) from TransactionEntity t")
    // Note: Hàm `latestTransactionId` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    Long latestTransactionId();

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Query("""
            select coalesce(sum(t.amount), 0)
            from TransactionEntity t
            join MilestoneEntity m on m.milestoneId = t.milestoneId
            join DisputeEntity d on d.milestoneId = m.milestoneId
            where t.status = 'Success'
              and t.transactionType = 'Deposit'
              and d.status in ('Open', 'UnderReview', 'Escalated')
            """)
    // Note: Hàm `calculateDisputedBalance` khai báo truy vấn dữ liệu để Spring Data JPA tự sinh logic truy cập database.
    BigDecimal calculateDisputedBalance();
}
