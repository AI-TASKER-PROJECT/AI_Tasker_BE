/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/ContractExecutionServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.ContractMilestoneEntity;
import com.aitasker.be.entity.DeliverableEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@ExtendWith(MockitoExtension.class)
class ContractExecutionServiceTest {

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AccessService accessService;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private BusinessProfileRepository businessProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ExpertProfileRepository expertProfileRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ProposalRepository proposalRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private JobRepository jobRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ContractRepository contractRepository;
    @Mock private ContractMilestoneRepository contractMilestoneRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private MilestoneAcceptanceCriteriaRepository milestoneCriteriaRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AcceptanceCriteriaRepository criteriaRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private DeliverableRepository deliverableRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private TransactionRepository transactionRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private DisputeRepository disputeRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private StaffRepository staffRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private SystemSettingRepository systemSettingRepository;
    @Mock private SystemWalletService systemWalletService;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @InjectMocks private ContractExecutionService contractExecutionService;

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    void signNda_shouldMoveContractToPendingWhenAllSignaturesExist() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1)
                .jobId(2)
                .businessId(10)
                .expertId(5)
                .totalBudget(BigDecimal.valueOf(1500))
                .status("DRAFT")
                .businessAcceptedAt(LocalDateTime.now().minusDays(1))
                .expertAcceptedAt(LocalDateTime.now().minusDays(1))
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractEntity saved = contractExecutionService.signNda(1);

        assertEquals("PENDING", saved.getStatus());
        assertNotNull(saved.getExpertNdaSignedAt());
        assertNull(saved.getActivatedAt());
    }

    @Test
    void rejectContract_shouldCancelContractAndReturnJobToProposalReview() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1)
                .jobId(2)
                .businessId(10)
                .expertId(5)
                .status("DRAFT")
                .build();
        JobEntity job = JobEntity.builder().jobId(2).businessId(10).status("OPEN").build();
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(jobRepository.findById(2)).thenReturn(Optional.of(job));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractEntity saved = contractExecutionService.rejectContract(1);

        assertEquals("CANCELLED", saved.getStatus());
        assertEquals("OPEN", job.getStatus());
        verify(jobRepository).save(job);
    }

    @Test
    void completeMilestone_shouldCompleteContractAndCloseJobWhenAllMilestonesCompleted() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1)
                .jobId(2)
                .businessId(10)
                .expertId(5)
                .status("ACTIVE")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(7)
                .jobId(2)
                .contractId(1)
                .status("UNDER_REVIEW")
                .build();
        JobEntity job = JobEntity.builder().jobId(2).businessId(10).status("IN_PROGRESS").build();
        when(milestoneRepository.findById(7)).thenReturn(Optional.of(milestone));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(milestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(milestone));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(2)).thenReturn(Optional.of(job));

        MilestoneEntity saved = contractExecutionService.completeMilestone(7);

        assertEquals("COMPLETED", saved.getStatus());
        assertEquals("COMPLETED", contract.getStatus());
        assertEquals("CLOSED", job.getStatus());
    }

    @Test
    void runSlaAutoApprove_shouldCompleteContractAndCloseJobWhenFinalMilestoneApproved() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1)
                .jobId(2)
                .businessId(10)
                .expertId(5)
                .status("ACTIVE")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(7)
                .jobId(2)
                .contractId(1)
                .status("UNDER_REVIEW")
                .build();
        DeliverableEntity deliverable = DeliverableEntity.builder()
                .deliverableId(20)
                .milestoneId(7)
                .createdAt(LocalDateTime.now().minusDays(8))
                .build();
        JobEntity job = JobEntity.builder().jobId(2).businessId(10).status("IN_PROGRESS").build();

        when(systemSettingRepository.findById("default_sla_days")).thenReturn(Optional.empty());
        when(milestoneRepository.findAll()).thenReturn(List.of(milestone));
        when(deliverableRepository.findByMilestoneId(7)).thenReturn(List.of(deliverable));
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(milestone));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(2)).thenReturn(Optional.of(job));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );

        List<MilestoneEntity> updated = contractExecutionService.runSlaAutoApprove();

        assertEquals(1, updated.size());
        assertEquals("COMPLETED", milestone.getStatus());
        assertEquals("COMPLETED", contract.getStatus());
        assertEquals("CLOSED", job.getStatus());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `updateTransactionStatus_shouldThrowWhenInvalidStatus` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void updateTransactionStatus_shouldThrowWhenInvalidStatus() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.updateTransactionStatus(1L, "INVALID"));
        assertEquals("STATUS TRANSACTION KHONG HOP LE", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `resolveDispute_shouldThrowWhenActionEmpty` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void resolveDispute_shouldThrowWhenActionEmpty() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.resolveDispute(1, " "));
        assertEquals("PROPOSED ACTION KHONG DUOC DE TRONG", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `signNda_shouldThrowWhenContractStatusInvalid` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void signNda_shouldThrowWhenContractNotActive() {
        ContractEntity contract = ContractEntity.builder().contractId(1).expertId(5).status("COMPLETED").build();
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(expertProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(com.aitasker.be.entity.ExpertProfileEntity.builder().expertId(5).build()));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.signNda(1));
        assertEquals("CONTRACT KHONG O TRANG THAI CHO PHEP KY NDA", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `terminateContract_shouldThrowWhenReasonBlank` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void terminateContract_shouldThrowWhenReasonBlank() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).status("ACTIVE").build();
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.terminateContract(1, " "));
        assertEquals("LY DO CHAM DUT KHONG DUOC DE TRONG", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `recordDemoTesting_shouldThrowWhenResultBlank` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void recordDemoTesting_shouldThrowWhenResultBlank() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.recordDemoTesting(1, " "));
        assertEquals("KET QUA DEMO TEST KHONG DUOC DE TRONG", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `issueTechnicalReport_shouldThrowWhenReportBlank` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void issueTechnicalReport_shouldThrowWhenReportBlank() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.issueTechnicalReport(1, " ", null));
        assertEquals("TECHNICAL REPORT KHONG DUOC DE TRONG", ex.getMessage());
    }

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `processPaymentWebhook_shouldThrowWhenStatusInvalid` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void processPaymentWebhook_shouldThrowWhenStatusInvalid() {
        AppException ex = assertThrows(AppException.class, () ->
                contractExecutionService.processPaymentWebhook(1L, "PENDING", null, null));
        assertEquals("PAYMENT STATUS KHONG HOP LE", ex.getMessage());
    }
}
