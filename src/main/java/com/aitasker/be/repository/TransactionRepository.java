package com.aitasker.be.repository;
import com.aitasker.be.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findByMilestoneId(Integer milestoneId);

    @Query("""
            select coalesce(sum(t.commissionFee), 0)
            from TransactionEntity t
            where t.status = 'Success'
            """)
    BigDecimal sumSuccessfulCommissionFee();

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
    BigDecimal calculateHoldingBalance();

    @Query("""
            select count(distinct c.businessId)
            from TransactionEntity t
            join MilestoneEntity m on m.milestoneId = t.milestoneId
            join ContractEntity c on c.jobId = m.jobId
            where t.status = 'Success' and t.transactionType = 'Deposit'
            """)
    long countDepositedBusinesses();

    long countByStatusAndTransactionType(String status, String transactionType);

    @Query("select coalesce(max(t.transactionId), 0) from TransactionEntity t")
    Long latestTransactionId();

    @Query("""
            select coalesce(sum(t.amount), 0)
            from TransactionEntity t
            join MilestoneEntity m on m.milestoneId = t.milestoneId
            join DisputeEntity d on d.milestoneId = m.milestoneId
            where t.status = 'Success'
              and t.transactionType = 'Deposit'
              and d.status in ('Open', 'UnderReview', 'Escalated')
            """)
    BigDecimal calculateDisputedBalance();
}
