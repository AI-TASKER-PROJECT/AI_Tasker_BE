/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/ContractExecutionServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.core.ContractMilestoneViewResponse;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.AcceptanceCriteriaEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.ContractMilestoneEntity;
import com.aitasker.be.entity.DeliverableEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneAcceptanceCriteriaEntity;
import com.aitasker.be.entity.MilestoneAcceptanceCriteriaId;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.ProposalEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doNothing;
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
    void terminateContract_byBusinessShouldNotifyExpert() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1)
                .businessId(10)
                .expertId(5)
                .status("DRAFT")
                .build();
        AccountEntity businessAccount = AccountEntity.builder()
                .accountId(99)
                .status("Approved")
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder()
                .expertId(5)
                .accountId(88)
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(businessAccount);
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(expert));

        contractExecutionService.terminateContract(1, "Business changes scope");

        verify(notificationService).notifyContractRejectedByBusiness(88, 99, 1, "Business changes scope");
    }

    @Test
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

    @Test
    void listMilestonesByContract_shouldUseLiveMilestoneStatusAfterDeliverableSubmission() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .milestoneName("Milestone A").description("Desc A")
                .originalBudget(BigDecimal.valueOf(500)).finalBudget(BigDecimal.valueOf(450))
                .orderIndex(1).status("PENDING")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(200).jobId(2).contractId(1)
                .milestoneName("Original Milestone A").description("Original Desc")
                .fundsAllocated(BigDecimal.valueOf(400)).orderIndex(1)
                .status("UNDER_REVIEW")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        ContractMilestoneViewResponse view = result.get(0);
        assertEquals("UNDER_REVIEW", view.getStatus());
        assertEquals("Milestone A", view.getMilestoneName());
        assertEquals("Desc A", view.getDescription());
        assertEquals(BigDecimal.valueOf(500), view.getOriginalBudget());
        assertEquals(BigDecimal.valueOf(450), view.getFinalBudget());
        assertEquals(1, view.getOrderIndex());
    }

    @Test
    void listMilestonesByContract_shouldUseLiveMilestoneStatusAfterMilestoneCompletion() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .milestoneName("Milestone B").description("Desc B")
                .originalBudget(BigDecimal.valueOf(600)).finalBudget(BigDecimal.valueOf(600))
                .orderIndex(2).status("PENDING")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(200).jobId(2).contractId(1)
                .status("COMPLETED")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
    }

    @Test
    void listMilestonesByContract_shouldUseLiveMilestoneStatusAfterSlaAutoApprove() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .milestoneName("Milestone C").orderIndex(1)
                .originalBudget(BigDecimal.valueOf(300)).finalBudget(BigDecimal.valueOf(300))
                .status("UNDER_REVIEW")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(200).jobId(2).contractId(1)
                .status("COMPLETED")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        assertEquals("COMPLETED", result.get(0).getStatus());
    }

    @Test
    void listMilestonesByContract_shouldFallbackToContractMilestoneStatusWhenLinkedMilestoneNotFound() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(999)
                .milestoneName("Deleted Milestone").description("No linked milestone")
                .originalBudget(BigDecimal.valueOf(200)).finalBudget(BigDecimal.valueOf(200))
                .orderIndex(1).status("PENDING")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(999)).thenReturn(Optional.empty());

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        ContractMilestoneViewResponse view = result.get(0);
        assertEquals("PENDING", view.getStatus());
        assertEquals("Deleted Milestone", view.getMilestoneName());
        assertEquals("No linked milestone", view.getDescription());
        assertEquals(BigDecimal.valueOf(200), view.getOriginalBudget());
        assertEquals(BigDecimal.valueOf(200), view.getFinalBudget());
    }

    @Test
    void listMilestonesByContract_shouldReturnSnapshotFieldsFromContractMilestones() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .milestoneName("Snapshot Name").description("Snapshot Desc")
                .originalBudget(BigDecimal.valueOf(1000)).finalBudget(BigDecimal.valueOf(800))
                .orderIndex(3).status("PENDING")
                .criteriaSnapshot("Snapshot criteria line 1\nSnapshot criteria line 2")
                .deliverableExpectation("Snapshot deliverable expectation")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(200).jobId(2).contractId(1)
                .milestoneName("Different Live Name").description("Different Live Desc")
                .fundsAllocated(BigDecimal.valueOf(500)).orderIndex(99)
                .status("COMPLETED")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        ContractMilestoneViewResponse view = result.get(0);
        assertEquals("COMPLETED", view.getStatus());
        assertEquals("Snapshot Name", view.getMilestoneName());
        assertEquals("Snapshot Desc", view.getDescription());
        assertEquals(BigDecimal.valueOf(1000), view.getOriginalBudget());
        assertEquals(BigDecimal.valueOf(800), view.getFinalBudget());
        assertEquals(3, view.getOrderIndex());
        assertEquals("Snapshot criteria line 1\nSnapshot criteria line 2", view.getCriteriaSnapshot());
        assertEquals("Snapshot deliverable expectation", view.getDeliverableExpectation());
    }

    @Test
    void listMilestonesByContract_shouldKeepSnapshotStableWhenMilestoneDescriptionChanges() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .milestoneName("Original Snapshot Name").description("Original Snapshot Desc")
                .originalBudget(BigDecimal.valueOf(1000)).finalBudget(BigDecimal.valueOf(1000))
                .orderIndex(1).status("PENDING")
                .criteriaSnapshot("Original criteria")
                .deliverableExpectation("Original deliverable expectation")
                .build();
        MilestoneEntity editedMilestone = MilestoneEntity.builder()
                .milestoneId(200).jobId(2).contractId(1)
                .milestoneName("Edited Name").description("Edited description after contract creation")
                .fundsAllocated(BigDecimal.valueOf(999)).orderIndex(1)
                .status("COMPLETED")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(editedMilestone));

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        ContractMilestoneViewResponse view = result.get(0);
        assertEquals("Original Snapshot Name", view.getMilestoneName());
        assertEquals("Original Snapshot Desc", view.getDescription());
        assertEquals("Original criteria", view.getCriteriaSnapshot());
        assertEquals("Original deliverable expectation", view.getDeliverableExpectation());
    }

    @Test
    void createDraftFromProposal_shouldCopySnapshotFieldsFromMilestoneAndAcceptanceCriteria() {
        JobEntity job = JobEntity.builder().jobId(2).businessId(10).title("Job A").build();
        ProposalEntity proposal = ProposalEntity.builder()
                .proposalId(50).jobId(2).expertId(5).status("Accepted")
                .proposalMilestone(null)
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(200).jobId(2)
                .milestoneName("M1").description("Deliverable expectation text")
                .fundsAllocated(BigDecimal.valueOf(500)).orderIndex(1)
                .status("PENDING").duration(7).durationUnit("DAY")
                .build();
        MilestoneAcceptanceCriteriaEntity link = MilestoneAcceptanceCriteriaEntity.builder()
                .id(new MilestoneAcceptanceCriteriaId(200, 301))
                .build();
        AcceptanceCriteriaEntity criteria = AcceptanceCriteriaEntity.builder()
                .criteriaId(301).criteriaCode("CODE_301")
                .description("Criteria snapshot text").isActive(true).sortOrder(1)
                .build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(proposalRepository.findById(50)).thenReturn(Optional.of(proposal));
        when(contractRepository.existsByProposalId(50)).thenReturn(false);
        when(jobRepository.findById(2)).thenReturn(Optional.of(job));
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(2)).thenReturn(List.of(milestone));
        when(milestoneCriteriaRepository.findByIdMilestoneId(200)).thenReturn(List.of(link));
        when(criteriaRepository.findById(301)).thenReturn(Optional.of(criteria));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> {
            ContractEntity saved = invocation.getArgument(0);
            saved.setContractId(1);
            return saved;
        });
        when(contractMilestoneRepository.save(any(ContractMilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(
                ContractMilestoneEntity.builder()
                        .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                        .milestoneName("M1").description("Deliverable expectation text")
                        .originalBudget(BigDecimal.valueOf(500)).finalBudget(BigDecimal.valueOf(500))
                        .orderIndex(1).status("PENDING")
                        .duration(7).durationUnit("DAY")
                        .criteriaSnapshot("Criteria snapshot text")
                        .deliverableExpectation("Deliverable expectation text")
                        .build()
        ));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.empty());

        ContractEntity saved = contractExecutionService.createDraftFromProposal(50, null);

        ArgumentCaptor<ContractMilestoneEntity> captor = forClass(ContractMilestoneEntity.class);
        verify(contractMilestoneRepository).save(captor.capture());
        ContractMilestoneEntity snapshot = captor.getValue();
        assertEquals("Criteria snapshot text", snapshot.getCriteriaSnapshot());
        assertEquals("Deliverable expectation text", snapshot.getDeliverableExpectation());
        assertEquals("M1", saved.getContractMilestones().get(0).getMilestoneName());
    }

    @Test
    void listMilestonesByContract_getDifference_shouldReturnCorrectValue() {
        ContractMilestoneViewResponse view = ContractMilestoneViewResponse.builder()
                .originalBudget(BigDecimal.valueOf(1000))
                .finalBudget(BigDecimal.valueOf(800))
                .build();

        assertEquals(BigDecimal.valueOf(-200), view.getDifference());
    }

    @Test
    void createMilestone_shouldThrowWhenDurationProvidedWithoutUnit() {
        JobEntity job = JobEntity.builder().jobId(1).businessId(10).status("DRAFT").build();
        MilestoneEntity input = MilestoneEntity.builder()
                .jobId(1).milestoneName("M1").fundsAllocated(BigDecimal.valueOf(100))
                .orderIndex(1).duration(5).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.createMilestone(input));
        assertEquals("DURATION VA DURATION UNIT PHAI CUNG CO HOAC CUNG KHONG CO", ex.getMessage());
    }

    @Test
    void createMilestone_shouldThrowWhenDurationUnitInvalid() {
        JobEntity job = JobEntity.builder().jobId(1).businessId(10).status("DRAFT").build();
        MilestoneEntity input = MilestoneEntity.builder()
                .jobId(1).milestoneName("M1").fundsAllocated(BigDecimal.valueOf(100))
                .orderIndex(1).duration(5).durationUnit("HOURS").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.createMilestone(input));
        assertEquals("DURATION UNIT KHONG HOP LE. CHAP NHAN: DAY, WEEK, MONTH", ex.getMessage());
    }

    @Test
    void createMilestone_shouldThrowWhenDurationNotPositive() {
        JobEntity job = JobEntity.builder().jobId(1).businessId(10).status("DRAFT").build();
        MilestoneEntity input = MilestoneEntity.builder()
                .jobId(1).milestoneName("M1").fundsAllocated(BigDecimal.valueOf(100))
                .orderIndex(1).duration(0).durationUnit("DAY").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.createMilestone(input));
        assertEquals("DURATION PHAI LON HON 0", ex.getMessage());
    }

    @Test
    void listMilestonesByContract_shouldReturnDurationFieldsFromContractMilestones() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .milestoneName("Snapshot").orderIndex(1)
                .originalBudget(BigDecimal.valueOf(500)).finalBudget(BigDecimal.valueOf(500))
                .duration(10).durationUnit("WEEK")
                .status("PENDING")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(200)).thenReturn(Optional.empty());

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        assertEquals(10, result.get(0).getDuration());
        assertEquals("WEEK", result.get(0).getDurationUnit());
    }

    @Test
    void updateMilestone_shouldThrowWhenMilestoneHasContract() {
        MilestoneEntity existing = MilestoneEntity.builder()
                .milestoneId(1).jobId(1).contractId(5).status("PENDING").build();
        MilestoneEntity input = MilestoneEntity.builder()
                .duration(10).durationUnit("DAY").build();

        when(milestoneRepository.findById(1)).thenReturn(Optional.of(existing));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.updateMilestone(1, input));
        assertEquals("KHONG THE SUA MILESTONE DA THUOC CONTRACT", ex.getMessage());
    }

    @Test
    void submitDeliverable_shouldNotifyBusinessWithCorrectTargetUrl() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1)
                .milestoneName("Milestone X").status("PENDING")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        DeliverableEntity input = DeliverableEntity.builder().milestoneId(10).build();
        DeliverableEntity saved = DeliverableEntity.builder()
                .deliverableId(100).milestoneId(10).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder()
                .businessId(10).accountId(20).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(deliverableRepository.save(any(DeliverableEntity.class))).thenReturn(saved);
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenReturn(milestone);
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));

        contractExecutionService.submitDeliverable(input);

        verify(notificationService).notifyDeliverableSubmitted(
                eq(20),
                eq(99),
                eq(1),
                eq(10),
                eq(100),
                eq("Milestone X")
        );
    }

    @Test
    void submitDeliverable_shouldSetMilestoneStatusToUnderReview() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1)
                .milestoneName("Milestone X").status("PENDING")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        DeliverableEntity input = DeliverableEntity.builder().milestoneId(10).build();
        DeliverableEntity saved = DeliverableEntity.builder()
                .deliverableId(100).milestoneId(10).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder()
                .businessId(10).accountId(20).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(deliverableRepository.save(any(DeliverableEntity.class))).thenReturn(saved);
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));

        contractExecutionService.submitDeliverable(input);

        ArgumentCaptor<MilestoneEntity> captor = forClass(MilestoneEntity.class);
        verify(milestoneRepository).save(captor.capture());
        assertEquals("UNDER_REVIEW", captor.getValue().getStatus());
    }

    @Test
    void listMilestonesByContract_shouldKeepDurationSnapshotStableWhenMilestoneDurationEdited() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .milestoneName("Snapshot").description("Snapshot Desc")
                .originalBudget(BigDecimal.valueOf(500)).finalBudget(BigDecimal.valueOf(500))
                .orderIndex(1).status("PENDING")
                .duration(10).durationUnit("WEEK")
                .build();
        MilestoneEntity editedMilestone = MilestoneEntity.builder()
                .milestoneId(200).jobId(2).contractId(1)
                .milestoneName("Edited").description("Edited desc")
                .fundsAllocated(BigDecimal.valueOf(999)).orderIndex(1)
                .status("COMPLETED")
                .duration(99).durationUnit("MONTH")
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(editedMilestone));

        List<ContractMilestoneViewResponse> result = contractExecutionService.listMilestonesByContract(1);

        assertEquals(1, result.size());
        ContractMilestoneViewResponse view = result.get(0);
        assertEquals(10, view.getDuration());
        assertEquals("WEEK", view.getDurationUnit());
    }
}
