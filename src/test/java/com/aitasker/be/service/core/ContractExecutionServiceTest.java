/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/ContractExecutionServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.core.AcceptanceCriteriaRequest;
import com.aitasker.be.dto.core.ContractMilestoneViewResponse;
import com.aitasker.be.dto.core.ImmediateTerminationRequest;
import com.aitasker.be.dto.core.ProgressReportFeedbackRequest;
import com.aitasker.be.dto.core.ProgressReportRequest;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.AcceptanceCriteriaEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.ContractMilestoneEntity;
import com.aitasker.be.entity.DeliverableEntity;
import com.aitasker.be.entity.DisputeEntity;
import com.aitasker.be.entity.ExpertProfileEntity;
import com.aitasker.be.entity.JobEntity;
import com.aitasker.be.entity.MilestoneProgressReportEntity;
import com.aitasker.be.entity.MilestoneEntity;
import com.aitasker.be.entity.ProposalEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.entity.StaffEntity;
import com.aitasker.be.entity.SystemSettingEntity;
import com.aitasker.be.entity.TerminationRequestEntity;
import com.aitasker.be.entity.WalletTransactionEntity;
import com.aitasker.be.entity.JobDomainEntity;
import com.aitasker.be.event.DisputeSettlementCompletedEvent;
import com.aitasker.be.entity.JobDomainId;
import com.aitasker.be.entity.DomainEntity;
import com.aitasker.be.entity.StaffDomainEntity;
import com.aitasker.be.entity.StaffDomainId;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContractExecutionServiceTest {

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AccessService accessService;
    @Mock private AccountRepository accountRepository;
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
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private AcceptanceCriteriaRepository criteriaRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private DeliverableRepository deliverableRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private TransactionRepository transactionRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private DisputeRepository disputeRepository;
    @Mock private TerminationRequestRepository terminationRequestRepository;
    @Mock private CaseAttachmentRepository caseAttachmentRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private MilestoneProgressReportRepository milestoneProgressReportRepository;
    @Mock private MilestoneProgressReportRequestRepository milestoneProgressReportRequestRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private StaffRepository staffRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private SystemSettingRepository systemSettingRepository;
    @Mock private SystemWalletService systemWalletService;
    @Mock private WalletLedgerService walletLedgerService;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private FirebaseStorageService firebaseStorageService;
    @Mock private ApplicationEventPublisher applicationEventPublisher;
    @Mock private JobDomainRepository jobDomainRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private StaffDomainRepository staffDomainRepository;
    @Mock private StaffSkillRepository staffSkillRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private SkillRepository skillRepository;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @InjectMocks private ContractExecutionService contractExecutionService;

    private void mockLockedContractMilestone(Integer contractId, Integer milestoneId, ContractMilestoneEntity contractMilestone) {
        when(contractMilestoneRepository.findByContractIdAndJobMilestoneIdForUpdate(contractId, milestoneId))
                .thenReturn(Optional.of(contractMilestone));
    }

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
        // completeMilestone giờ ủy quyền cho approveMilestone (release escrow) theo spec 9.4.
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(70).contractId(1).jobMilestoneId(7)
                .finalBudget(BigDecimal.valueOf(1000)).status("UNDER_REVIEW")
                .build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder().expertId(5).accountId(60).build();
        JobEntity job = JobEntity.builder().jobId(2).businessId(10).status("IN_PROGRESS").build();
        when(milestoneRepository.findById(7)).thenReturn(Optional.of(milestone));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(business));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(expert));
        mockLockedContractMilestone(1, 7, cm);
        when(contractMilestoneRepository.save(any(ContractMilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(disputeRepository.findByMilestoneIdAndStatusIn(eq(7), any())).thenReturn(List.of());
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(milestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(milestone));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jobRepository.findById(2)).thenReturn(Optional.of(job));

        MilestoneEntity saved = contractExecutionService.completeMilestone(7);

        assertEquals("COMPLETED", saved.getStatus());
        assertEquals("COMPLETED", contract.getStatus());
        assertEquals("CLOSED", job.getStatus());
        verify(walletLedgerService).debitEscrow(eq(50), eq(BigDecimal.valueOf(1000)), any(), any(), eq(7L), any(), any(WalletLedgerService.WalletOperationContext.class));
        verify(walletLedgerService).creditAvailable(eq(60), eq(BigDecimal.valueOf(1000)), any(), any(), eq(7L), any(), any(WalletLedgerService.WalletOperationContext.class));
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
        ContractMilestoneEntity contractMilestone = ContractMilestoneEntity.builder()
                .contractMilestoneId(70).contractId(1).jobMilestoneId(7)
                .finalBudget(BigDecimal.valueOf(1000)).status("UNDER_REVIEW").build();
        JobEntity job = JobEntity.builder().jobId(2).businessId(10).status("IN_PROGRESS").build();

        when(systemSettingRepository.findById("default_sla_days")).thenReturn(Optional.empty());
        when(milestoneRepository.findAll()).thenReturn(List.of(milestone));
        when(deliverableRepository.findByMilestoneId(7)).thenReturn(List.of(deliverable));
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        mockLockedContractMilestone(1, 7, contractMilestone);
        when(businessProfileRepository.findById(10))
                .thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).accountId(50).build()));
        when(expertProfileRepository.findById(5))
                .thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).accountId(60).build()));
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
                .status(ContractEntity.STATUS_ACTIVE)
                .build();
        AccountEntity businessAccount = AccountEntity.builder()
                .accountId(99)
                .status("Approved")
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(businessAccount);
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.empty());
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(terminationRequestRepository.findByContractIdAndStatusIn(eq(1), any())).thenReturn(List.of());
        when(terminationRequestRepository.save(any(TerminationRequestEntity.class))).thenAnswer(invocation -> {
            TerminationRequestEntity request = invocation.getArgument(0);
            request.setTerminationRequestId(1L);
            return request;
        });
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractEntity result = contractExecutionService.terminateContract(1, "Business changes scope");

        assertEquals(ContractEntity.STATUS_TERMINATION_PENDING, result.getStatus());
        assertEquals("Business changes scope", result.getTerminationReason());
        verify(notificationService, never()).notifyContractRejectedByBusiness(any(), any(), any(), any());
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
        AcceptanceCriteriaEntity criteria = AcceptanceCriteriaEntity.builder()
                .criteriaId(301).milestoneId(200)
                .description("Criteria snapshot text").sortOrder(1)
                .build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(proposalRepository.findById(50)).thenReturn(Optional.of(proposal));
        when(contractRepository.existsByProposalId(50)).thenReturn(false);
        when(jobRepository.findById(2)).thenReturn(Optional.of(job));
        when(milestoneRepository.findByJobIdOrderByOrderIndexAsc(2)).thenReturn(List.of(milestone));
        when(criteriaRepository.findByMilestoneIdOrderBySortOrderAscCriteriaIdAsc(200))
                .thenReturn(List.of(criteria));
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
    void updateMilestone_whenReplacingCriteriaAfterContractCreation_shouldRejectMutation() {
        MilestoneEntity existing = MilestoneEntity.builder()
                .milestoneId(5).jobId(1).milestoneName("M1")
                .fundsAllocated(BigDecimal.TEN).status("PENDING").build();
        MilestoneEntity input = MilestoneEntity.builder()
                .acceptanceCriteria(List.of("Changed after snapshot")).build();
        JobEntity job = JobEntity.builder()
                .jobId(1).businessId(10).status("OPEN").build();
        AccountEntity actor = AccountEntity.builder()
                .accountId(99)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();

        when(milestoneRepository.findById(5)).thenReturn(Optional.of(existing));
        when(accessService.currentAccount()).thenReturn(actor);
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        when(businessProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findByJobId(1)).thenReturn(Optional.of(
                ContractEntity.builder().contractId(3).jobId(1).build()));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.updateMilestone(5, input));

        assertEquals("JOB DA CO CONTRACT, KHONG DUOC SUA TIEU CHI NGHIEM THU", ex.getMessage());
        verify(milestoneRepository, org.mockito.Mockito.never()).save(any(MilestoneEntity.class));
    }

    @Test
    void criterionCrud_shouldRequireOwnedPreContractMilestoneAndPersistChanges() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(5).jobId(1).milestoneName("M1").build();
        JobEntity job = JobEntity.builder()
                .jobId(1).businessId(10).status("DRAFT").build();
        AccountEntity actor = AccountEntity.builder()
                .accountId(99)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();
        AcceptanceCriteriaEntity existing = AcceptanceCriteriaEntity.builder()
                .criteriaId(7).milestoneId(5).description("Old").sortOrder(1).build();

        when(accessService.currentAccount()).thenReturn(actor);
        when(milestoneRepository.findById(5)).thenReturn(Optional.of(milestone));
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        when(businessProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findByJobId(1)).thenReturn(Optional.empty());
        when(criteriaRepository.findByMilestoneIdOrderBySortOrderAscCriteriaIdAsc(5))
                .thenReturn(List.of(existing));
        when(criteriaRepository.findById(7)).thenReturn(Optional.of(existing));
        when(criteriaRepository.save(any(AcceptanceCriteriaEntity.class))).thenAnswer(invocation -> {
            AcceptanceCriteriaEntity saved = invocation.getArgument(0);
            if (saved.getCriteriaId() == null) saved.setCriteriaId(8);
            return saved;
        });

        AcceptanceCriteriaEntity created = contractExecutionService.createCriteria(
                5, new AcceptanceCriteriaRequest("New criterion", null));
        AcceptanceCriteriaEntity updated = contractExecutionService.updateCriteria(
                5, 7, new AcceptanceCriteriaRequest("Updated criterion", 2));
        contractExecutionService.deleteCriteria(5, 7);

        assertEquals(5, created.getMilestoneId());
        assertEquals(2, created.getSortOrder());
        assertEquals("Updated criterion", updated.getDescription());
        assertEquals(2, updated.getSortOrder());
        verify(criteriaRepository).delete(existing);
    }

    @Test
    void createCriterion_whenJobHasContract_shouldRejectMutation() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(5).jobId(1).milestoneName("M1").build();
        JobEntity job = JobEntity.builder()
                .jobId(1).businessId(10).status("OPEN").build();
        AccountEntity actor = AccountEntity.builder()
                .accountId(99)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();

        when(accessService.currentAccount()).thenReturn(actor);
        when(milestoneRepository.findById(5)).thenReturn(Optional.of(milestone));
        when(jobRepository.findById(1)).thenReturn(Optional.of(job));
        when(businessProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findByJobId(1)).thenReturn(Optional.of(
                ContractEntity.builder().contractId(3).jobId(1).build()));

        AppException ex = assertThrows(AppException.class, () ->
                contractExecutionService.createCriteria(
                        5, new AcceptanceCriteriaRequest("Should fail", 1)));

        assertEquals("JOB DA CO CONTRACT, KHONG DUOC SUA TIEU CHI NGHIEM THU", ex.getMessage());
    }

    @Test
    void submitDeliverable_shouldNotifyBusinessWithCorrectTargetUrl() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1)
                .milestoneName("Milestone X").status("IN_PROGRESS")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10).status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusHours(1))
                .duration(1).durationUnit("DAY")
                .build();
        DeliverableEntity input = DeliverableEntity.builder().milestoneId(10)
                .sourceCodeUrl("https://github.com/expert/project").build();
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
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
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
                .milestoneName("Milestone X").status("IN_PROGRESS")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10).status("IN_PROGRESS")
                .build();
        DeliverableEntity input = DeliverableEntity.builder().milestoneId(10)
                .sourceCodeUrl("https://github.com/expert/project").build();
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
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(deliverableRepository.save(any(DeliverableEntity.class))).thenReturn(saved);
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));

        contractExecutionService.submitDeliverable(input);

        ArgumentCaptor<MilestoneEntity> captor = forClass(MilestoneEntity.class);
        verify(milestoneRepository).save(captor.capture());
        assertEquals("UNDER_REVIEW", captor.getValue().getStatus());
    }

    @Test
    void submitDeliverable_shouldRejectDepositedMilestoneBeforeExpertStartsWork() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1)
                .milestoneName("Milestone X").status("DEPOSITED")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10).status("DEPOSITED")
                .build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.submitDeliverable(DeliverableEntity.builder().milestoneId(10)
                        .sourceCodeUrl("https://github.com/expert/project").build()));

        assertEquals("MILESTONE CHUA SAN SANG DE SUBMIT DELIVERABLE", ex.getMessage());
        verify(deliverableRepository, never()).save(any());
    }

    @Test
    void submitDeliverable_shouldAllowNormalResubmissionAfterRejection() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1)
                .milestoneName("Milestone X").status("IN_PROGRESS")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10).status("IN_PROGRESS")
                .build();
        DeliverableEntity saved = DeliverableEntity.builder().deliverableId(100).milestoneId(10).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(deliverableRepository.findByMilestoneIdOrderBySubmissionRoundDesc(10)).thenReturn(List.of());
        when(deliverableRepository.save(any(DeliverableEntity.class))).thenReturn(saved);
        when(milestoneRepository.save(any(MilestoneEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).accountId(20).build()));

        contractExecutionService.submitDeliverable(DeliverableEntity.builder().milestoneId(10)
                .sourceCodeFileUrl("milestone-source-code/milestones/10/accounts/99/source.zip").build());

        assertEquals("UNDER_REVIEW", milestone.getStatus());
        assertEquals("UNDER_REVIEW", cm.getStatus());
    }

    @Test
    void submitDeliverable_shouldRejectWhenRepositoryUrlAndFileAreBothMissing() {
        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.submitDeliverable(
                        DeliverableEntity.builder().milestoneId(10).build()));

        assertEquals("PHAI CUNG CAP SOURCE CODE URL HOAC FILE SOURCE CODE", ex.getMessage());
        verify(milestoneRepository, never()).findById(any());
        verify(deliverableRepository, never()).save(any());
    }

    @Test
    void submitDeliverable_shouldRejectLateResubmissionEvenWhenStatusIsStillInProgress() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1)
                .milestoneName("Milestone X").status("IN_PROGRESS")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10)
                .status("IN_PROGRESS").resubmitCount(1)
                .inProgressStartedAt(LocalDateTime.now().minusDays(2))
                .duration(1).durationUnit("DAY")
                .build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99)
                        .role(RoleEntity.builder().roleName("EXPERT").build()).build());
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.submitDeliverable(DeliverableEntity.builder()
                        .milestoneId(10).sourceCodeUrl("https://github.com/expert/project").build()));

        assertEquals("MILESTONE_DA_QUA_HAN_NOP_SAN_PHAM", ex.getMessage());
        verify(deliverableRepository, never()).save(any());
        verify(milestoneRepository, never()).save(any());
        verifyNoInteractions(notificationService);
    }

    @Test
    void submitDeliverable_shouldRejectMilestoneAlreadyMarkedOverdue() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1)
                .milestoneName("Milestone X").status("OVERDUE")
                .build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10)
                .status("OVERDUE")
                .build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99)
                        .role(RoleEntity.builder().roleName("EXPERT").build()).build());
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.submitDeliverable(DeliverableEntity.builder()
                        .milestoneId(10).sourceCodeUrl("https://github.com/expert/project").build()));

        assertEquals("MILESTONE_DA_QUA_HAN_NOP_SAN_PHAM", ex.getMessage());
        verify(deliverableRepository, never()).save(any());
        verify(milestoneRepository, never()).save(any());
    }

    @Test
    void submitDeliverable_shouldRejectSourceArchiveFromAnotherExpertFolder() {
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99)
                        .role(RoleEntity.builder().roleName("EXPERT").build()).build());
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(
                MilestoneEntity.builder().milestoneId(10).jobId(2).build()));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.submitDeliverable(DeliverableEntity.builder()
                        .milestoneId(10)
                        .sourceCodeFileUrl("milestone-source-code/milestones/10/accounts/88/source.zip")
                        .build()));

        assertEquals("FILE SOURCE CODE KHONG THUOC MILESTONE HOAC EXPERT HIEN TAI", ex.getMessage());
        verify(deliverableRepository, never()).save(any());
    }

    @Test
    void uploadMilestoneSourceCode_shouldDelegateToDedicatedZipStorageAndAudit() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1).status("IN_PROGRESS").build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10)
                .status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusHours(1))
                .duration(1).durationUnit("DAY")
                .build();
        AccountEntity actor = AccountEntity.builder().accountId(99)
                .role(RoleEntity.builder().roleName("EXPERT").build()).build();
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        String expectedPath = "milestone-source-code/milestones/10/accounts/99/source.zip";

        when(accessService.currentAccount()).thenReturn(actor);
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(firebaseStorageService.uploadSourceCodeArchive(
                file, "milestone-source-code/milestones/10/accounts/99"))
                .thenReturn(expectedPath);

        String result = contractExecutionService.uploadMilestoneSourceCode(10, file);

        assertEquals(expectedPath, result);
        verify(firebaseStorageService).uploadSourceCodeArchive(
                file, "milestone-source-code/milestones/10/accounts/99");
        verify(auditLogService).record(
                AuditLogService.ACTION_UPLOAD_MILESTONE_SOURCE_CODE,
                "milestones", "10", 99);
    }

    @Test
    void uploadMilestoneSourceCode_shouldRejectAfterDeadlineWithoutUploading() {
        MilestoneEntity milestone = MilestoneEntity.builder()
                .milestoneId(10).jobId(2).contractId(1).status("IN_PROGRESS").build();
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .businessNdaSignedAt(LocalDateTime.now().minusDays(1))
                .expertNdaSignedAt(LocalDateTime.now().minusDays(1))
                .status("ACTIVE")
                .build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(10)
                .status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusDays(8))
                .duration(1).durationUnit("WEEK")
                .build();
        AccountEntity actor = AccountEntity.builder().accountId(99)
                .role(RoleEntity.builder().roleName("EXPERT").build()).build();
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);

        when(accessService.currentAccount()).thenReturn(actor);
        when(milestoneRepository.findById(10)).thenReturn(Optional.of(milestone));
        when(contractRepository.findByJobId(2)).thenReturn(Optional.of(contract));
        when(expertProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.uploadMilestoneSourceCode(10, file));

        assertEquals("MILESTONE_DA_QUA_HAN_NOP_SAN_PHAM", ex.getMessage());
        verifyNoInteractions(firebaseStorageService);
        verifyNoInteractions(auditLogService);
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

    // --- Phase 2 Dispute Flow Tests ---

    @Test
    void depositMilestoneEscrow_shouldHoldEscrowAndAutoStartMilestone() {
        ContractEntity contract = ContractEntity.builder().contractId(1).jobId(2).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).milestoneName("M1").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .finalBudget(BigDecimal.valueOf(1000)).status("PENDING")
                .build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(business));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        mockLockedContractMilestone(1, 200, cm);
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MilestoneEntity result = contractExecutionService.depositMilestoneEscrow(1, 200);

        verify(walletLedgerService).holdEscrowFromAvailable(eq(50), eq(BigDecimal.valueOf(1000)), any(), any(), eq(200L), any(), any(WalletLedgerService.WalletOperationContext.class));
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals("IN_PROGRESS", cm.getStatus());
        assertNotNull(cm.getInProgressStartedAt());
    }

    @Test
    void approveMilestone_shouldDebitEscrowAndCreditExpert() {
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).contractId(1).status("UNDER_REVIEW").build();
        ContractEntity contract = ContractEntity.builder().contractId(1).jobId(2).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .finalBudget(BigDecimal.valueOf(1000)).status("UNDER_REVIEW")
                .build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder().expertId(5).accountId(60).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(business));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(expert));
        mockLockedContractMilestone(1, 200, cm);
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MilestoneEntity result = contractExecutionService.approveMilestone(200);

        verify(walletLedgerService).debitEscrow(eq(50), eq(BigDecimal.valueOf(1000)), any(), any(), eq(200L), any(), any(WalletLedgerService.WalletOperationContext.class));
        verify(walletLedgerService).creditAvailable(eq(60), eq(BigDecimal.valueOf(1000)), any(), any(), eq(200L), any(), any(WalletLedgerService.WalletOperationContext.class));
        assertEquals("COMPLETED", result.getStatus());
        assertEquals("COMPLETED", cm.getStatus());
        assertNotNull(cm.getEscrowReleasedAt());
    }

    @Test
    void rejectMilestone_shouldIncrementResubmitCount() {
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).contractId(1).status("UNDER_REVIEW").build();
        ContractEntity contract = ContractEntity.builder().contractId(1).jobId(2).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .finalBudget(BigDecimal.valueOf(1000)).resubmitCount(0).status("UNDER_REVIEW")
                .build();
        DeliverableEntity deliverable = DeliverableEntity.builder()
                .deliverableId(300).milestoneId(200).submissionRound(1).status("SUBMITTED").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(deliverableRepository.findByMilestoneIdOrderBySubmissionRoundDesc(200)).thenReturn(List.of(deliverable));
        when(disputeRepository.findByMilestoneIdAndStatusIn(eq(200), any())).thenReturn(List.of());
        when(deliverableRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MilestoneEntity result = contractExecutionService.rejectMilestone(200, "Not good enough");

        assertEquals(Integer.valueOf(1), cm.getResubmitCount());
        assertEquals("IN_PROGRESS", result.getStatus());
        assertEquals("REJECTED", deliverable.getStatus());
        assertEquals("Not good enough", deliverable.getRejectionFeedback());
        verify(disputeRepository, never()).save(any());
    }

    @Test
    void initiateDispute_shouldCreateDisputeAndSetMilestoneDisputed() {
        ContractEntity contract = ContractEntity.builder().contractId(1).jobId(2).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).contractId(1).status("IN_PROGRESS").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200).status("IN_PROGRESS")
                .build();
        DisputeEntity savedDispute = DisputeEntity.builder().disputeId(1).contractId(1).milestoneId(200).initiatedBy("BUSINESS").status("PENDING_SELF_RESOLVE").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(disputeRepository.findByMilestoneIdAndStatusIn(eq(200), any())).thenReturn(List.of());
        when(disputeRepository.save(any())).thenReturn(savedDispute);

        DisputeEntity result = contractExecutionService.initiateDispute(1, 200, "BUSINESS", null);

        assertEquals("DISPUTED", milestone.getStatus());
        assertEquals("DISPUTED", cm.getStatus());
        assertEquals("BUSINESS", result.getInitiatedBy());
        assertEquals("PENDING_SELF_RESOLVE", result.getStatus());
    }

    @Test
    void initiateDispute_shouldRejectSpoofedInitiatorWithoutChangingMilestoneStatus() {
        ContractEntity contract = ContractEntity.builder().contractId(1).jobId(2).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).contractId(1).status("IN_PROGRESS").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(disputeRepository.findByMilestoneIdAndStatusIn(eq(200), any())).thenReturn(List.of());

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.initiateDispute(1, 200, "BUSINESS", null));

        assertEquals("INITIATED_BY PHAI KHOP VOI VAI TRO NGUOI DUNG HIEN TAI", ex.getMessage());
        assertEquals("IN_PROGRESS", milestone.getStatus());
        verify(milestoneRepository, never()).save(any());
        verify(disputeRepository, never()).save(any());
    }

    @Test
    void submitProgressReport_shouldRejectMilestoneOutsideContract() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(2).status("IN_PROGRESS").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> contractExecutionService.submitProgressReport(1, 200, "Progress", 50, null));

        verify(milestoneProgressReportRepository, never()).save(any());
    }

    @Test
    void submitProgressReport_shouldUseNullCheckpointWhenDurationMissing() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(1).milestoneName("M1").status("IN_PROGRESS").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findByMilestoneIdOrderByCreatedAtAsc(200)).thenReturn(List.of());
        when(milestoneProgressReportRepository.save(any(MilestoneProgressReportEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProgressReportRequest request = new ProgressReportRequest();
        request.setContent("Progress");
        request.setPercentComplete(50);
        request.setSourceCodeFileUrl("milestone-source-code/milestones/200/source.zip");

        MilestoneProgressReportEntity result = contractExecutionService.submitProgressReport(1, 200, request);

        assertNull(result.getCheckpointType());
        assertFalse(result.getIsLate());
        assertEquals("milestone-source-code/milestones/200/source.zip", result.getSourceCodeFileUrl());
    }

    @Test
    void escalateDispute_shouldAutoRouteToStaffAndNotify() {
        DisputeEntity dispute = DisputeEntity.builder().disputeId(1).contractId(1).milestoneId(200).status("PENDING_SELF_RESOLVE").build();
        StaffEntity staff = StaffEntity.builder().staffId(9).accountId(5).build();
        DomainEntity domain = DomainEntity.builder().domainId(2).domainCode("GEN_AI").domainName("Gen AI").isActive(true).sortOrder(1).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(contractRepository.findById(1)).thenReturn(Optional.of(ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").jobId(50).build()));
        when(disputeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(staffRepository.findAllForDisputeRouting()).thenReturn(List.of(staff));
        when(staffRepository.findById(9)).thenReturn(Optional.of(staff));
        when(jobDomainRepository.findByIdJobId(50)).thenReturn(List.of(
                new JobDomainEntity(new JobDomainId(50, 2), LocalDateTime.now())));
        when(jobSkillRepository.findByIdJobId(50)).thenReturn(List.of());
        when(staffDomainRepository.findByIdStaffId(9)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(9, 2))));
        when(staffSkillRepository.findByIdStaffId(9)).thenReturn(List.of());
        when(domainRepository.findAllById(List.of(2))).thenReturn(List.of(domain));
        when(skillRepository.findAllById(any())).thenReturn(List.of());
        when(disputeRepository.findByAssignedStaffId(any())).thenReturn(List.of());
        when(accountRepository.findById(5)).thenReturn(Optional.of(AccountEntity.builder().accountId(5).fullName("Staff")
                .role(RoleEntity.builder().roleName("STAFF").build()).status("Approved").build()));

        DisputeEntity result = contractExecutionService.escalateDispute(1, "They cheated", "evidence.pdf");

        assertEquals(DisputeEntity.STATUS_STAFF_REVIEWING, result.getStatus());
        assertEquals("They cheated", result.getEscalationReason());
        assertEquals("evidence.pdf", result.getEscalationEvidenceFile());
        assertEquals(Integer.valueOf(9), result.getAssignedStaffId());
        verify(notificationService).notifyDisputeEscalationRequested(eq(5), eq(99), eq(1));
        verify(notificationService).notifyDisputeAssigned(eq(5), eq(99), eq(1));
    }

    @Test
    void escalateDispute_shouldRemainRequestedWhenQualifiedStaffIsAtCapacity() {
        DisputeEntity dispute = DisputeEntity.builder().disputeId(1).contractId(1).milestoneId(200)
                .status(DisputeEntity.STATUS_PENDING_SELF_RESOLVE).build();
        StaffEntity staff = StaffEntity.builder().staffId(9).accountId(5).build();
        DomainEntity domain = DomainEntity.builder().domainId(2).domainCode("GEN_AI")
                .domainName("Gen AI").isActive(true).sortOrder(1).build();
        SystemSettingEntity capacity = SystemSettingEntity.builder()
                .settingKey("dispute_staff_max_active_cases").settingValue("5")
                .valueType("INT").isActive(true).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(contractRepository.findById(1)).thenReturn(Optional.of(ContractEntity.builder()
                .contractId(1).businessId(10).expertId(5).status("ACTIVE").jobId(50).build()));
        when(disputeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(staffRepository.findAllForDisputeRouting()).thenReturn(List.of(staff));
        when(jobDomainRepository.findByIdJobId(50)).thenReturn(List.of(
                new JobDomainEntity(new JobDomainId(50, 2), LocalDateTime.now())));
        when(jobSkillRepository.findByIdJobId(50)).thenReturn(List.of());
        when(staffDomainRepository.findByIdStaffId(9)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(9, 2))));
        when(staffSkillRepository.findByIdStaffId(9)).thenReturn(List.of());
        when(domainRepository.findAllById(List.of(2))).thenReturn(List.of(domain));
        when(skillRepository.findAllById(any())).thenReturn(List.of());
        when(accountRepository.findById(5)).thenReturn(Optional.of(AccountEntity.builder()
                .accountId(5).fullName("Staff").role(RoleEntity.builder().roleName("STAFF").build())
                .status("Approved").build()));
        when(systemSettingRepository.findById("dispute_staff_max_active_cases"))
                .thenReturn(Optional.of(capacity));
        when(disputeRepository.countByAssignedStaffIdAndStatusIn(eq(9), any())).thenReturn(5L);

        DisputeEntity result = contractExecutionService.escalateDispute(1, "Need intervention", null);

        assertEquals(DisputeEntity.STATUS_ESCALATION_REQUESTED, result.getStatus());
        assertNull(result.getAssignedStaffId());
        verify(notificationService, never()).notifyDisputeAssigned(anyInt(), anyInt(), anyInt());
    }

    @Test
    void staffDecide_shouldTriggerInlineSettlementAndCompleteMilestone() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        DisputeEntity dispute = DisputeEntity.builder().disputeId(1).contractId(1).milestoneId(200).assignedStaffId(9).status("STAFF_REVIEWING").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .finalBudget(BigDecimal.valueOf(1000)).status("DISPUTED")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).status("DISPUTED").build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder().expertId(5).accountId(60).build();
        WalletTransactionEntity debit = WalletTransactionEntity.builder().id(77L).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("STAFF").build()).build()
        );
        when(staffRepository.findByAccountId(1)).thenReturn(Optional.of(StaffEntity.builder().staffId(9).build()));
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        mockLockedContractMilestone(1, 200, cm);
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(expert));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contractRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(walletLedgerService.debitEscrow(eq(50), eq(BigDecimal.valueOf(1000)), any(), any(), eq(1L), any(), any(WalletLedgerService.WalletOperationContext.class))).thenReturn(debit);
        when(accountRepository.findAllByRoleRoleNameOrderByAccountIdAsc("ADMIN")).thenReturn(List.of(
                AccountEntity.builder().accountId(80).build(),
                AccountEntity.builder().accountId(81).build()));

        DisputeEntity result = contractExecutionService.staffDecide(1, 60, "Partial fault", null);

        assertEquals(DisputeEntity.STATUS_RESOLVED, result.getStatus());
        assertEquals(Integer.valueOf(60), result.getStaffDecisionPercentage());
        verify(walletLedgerService).debitEscrow(eq(50), eq(BigDecimal.valueOf(1000)), any(), any(), eq(1L), any(), any(WalletLedgerService.WalletOperationContext.class));
        verify(walletLedgerService).creditAvailable(eq(60), eq(BigDecimal.valueOf(600)), any(), any(), eq(1L), any(), any(WalletLedgerService.WalletOperationContext.class));
        verify(walletLedgerService).creditAvailable(eq(50), eq(BigDecimal.valueOf(400)), any(), any(), eq(1L), any(), any(WalletLedgerService.WalletOperationContext.class));
        assertEquals("COMPLETED", milestone.getStatus());
        assertEquals("COMPLETED", cm.getStatus());
        ArgumentCaptor<DisputeSettlementCompletedEvent> eventCaptor = ArgumentCaptor.forClass(DisputeSettlementCompletedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertEquals(List.of(80, 81), eventCaptor.getValue().adminAccountIds());
        assertEquals(new BigDecimal("600"), eventCaptor.getValue().expertPayoutAmount());
        assertEquals(77L, eventCaptor.getValue().settlementWalletTransactionId());
    }

    @Test
    void staffDecide_shouldByPassEvidenceWindowDeadline() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        DisputeEntity dispute = DisputeEntity.builder().disputeId(1).contractId(1).milestoneId(200).assignedStaffId(9).status("STAFF_REVIEWING")
                .evidenceCollectionDueAt(LocalDateTime.now().plusHours(24)).build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .finalBudget(BigDecimal.valueOf(500)).status("DISPUTED")
                .build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).status("DISPUTED").build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder().expertId(5).accountId(60).build();
        WalletTransactionEntity debit = WalletTransactionEntity.builder().id(88L).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("STAFF").build()).build());
        when(staffRepository.findByAccountId(1)).thenReturn(Optional.of(StaffEntity.builder().staffId(9).build()));
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        mockLockedContractMilestone(1, 200, cm);
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(expert));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contractRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(walletLedgerService.debitEscrow(eq(50), eq(BigDecimal.valueOf(500)), any(), any(), eq(1L), any(), any(WalletLedgerService.WalletOperationContext.class))).thenReturn(debit);

        DisputeEntity result = contractExecutionService.staffDecide(1, 100, "Early decision", null);

        assertEquals(DisputeEntity.STATUS_RESOLVED, result.getStatus());
        assertEquals("COMPLETED", milestone.getStatus());
    }

    @Test
    void executeDisputeSettlement_shouldRequireStaffDecidedStatus() {
        DisputeEntity dispute = DisputeEntity.builder().disputeId(1).status("PENDING_SELF_RESOLVE").build();
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build());
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.executeDisputeSettlement(1));
        assertEquals("DISPUTE_NOT_STAFF_DECIDED", ex.getMessage());
    }

    @Test
    void requestTermination_byParticipantSetsTerminationPending() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1)
                .businessId(10)
                .expertId(5)
                .status(ContractEntity.STATUS_ACTIVE)
                .build();
        AccountEntity businessActor = AccountEntity.builder()
                .accountId(50)
                .role(RoleEntity.builder().roleName("BUSINESS").build())
                .build();

        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(businessActor);
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(expertProfileRepository.findByAccountId(50)).thenReturn(Optional.empty());
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(terminationRequestRepository.findByContractIdAndStatusIn(eq(1), any())).thenReturn(List.of());
        when(terminationRequestRepository.save(any(TerminationRequestEntity.class))).thenAnswer(invocation -> {
            TerminationRequestEntity request = invocation.getArgument(0);
            request.setTerminationRequestId(1L);
            return request;
        });
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(i -> i.getArgument(0));

        ContractEntity result = contractExecutionService.requestTermination(1, "Scope changed");

        assertEquals(ContractEntity.STATUS_TERMINATION_PENDING, result.getStatus());
        assertEquals("Scope changed", result.getTerminationReason());
        verify(auditLogService).record("TERMINATION_REQUESTED", "termination_requests", "1", 50);
    }

    @Test
    void executeTermination_refundsEscrowAndCancelsActiveMilestones() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status(ContractEntity.STATUS_TERMINATION_PENDING).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();
        ContractMilestoneEntity deposited = ContractMilestoneEntity.builder().contractMilestoneId(101).contractId(1).jobMilestoneId(201).finalBudget(BigDecimal.valueOf(100)).status(ContractMilestoneEntity.STATUS_DEPOSITED).build();
        ContractMilestoneEntity inProgress = ContractMilestoneEntity.builder().contractMilestoneId(102).contractId(1).jobMilestoneId(202).finalBudget(BigDecimal.valueOf(200)).status(ContractMilestoneEntity.STATUS_IN_PROGRESS).build();
        ContractMilestoneEntity underReview = ContractMilestoneEntity.builder().contractMilestoneId(103).contractId(1).jobMilestoneId(203).finalBudget(BigDecimal.valueOf(300)).status(ContractMilestoneEntity.STATUS_UNDER_REVIEW).build();
        ContractMilestoneEntity disputed = ContractMilestoneEntity.builder().contractMilestoneId(104).contractId(1).jobMilestoneId(204).finalBudget(BigDecimal.valueOf(400)).status(ContractMilestoneEntity.STATUS_DISPUTED).build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build());
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(deposited, inProgress, underReview, disputed));
        when(contractMilestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(i -> i.getArgument(0));

        ContractEntity result = contractExecutionService.executeTermination(1, "Admin approved closure");

        verify(walletLedgerService).releaseEscrowToAvailable(eq(50), eq(BigDecimal.valueOf(100)), eq("MILESTONE_ESCROW_REFUND"), eq("MILESTONE"), eq(201L), eq("Refund escrow on contract termination"));
        verify(walletLedgerService).releaseEscrowToAvailable(eq(50), eq(BigDecimal.valueOf(200)), eq("MILESTONE_ESCROW_REFUND"), eq("MILESTONE"), eq(202L), eq("Refund escrow on contract termination"));
        verify(walletLedgerService).releaseEscrowToAvailable(eq(50), eq(BigDecimal.valueOf(300)), eq("MILESTONE_ESCROW_REFUND"), eq("MILESTONE"), eq(203L), eq("Refund escrow on contract termination"));
        verify(walletLedgerService, never()).releaseEscrowToAvailable(eq(50), eq(BigDecimal.valueOf(400)), any(), any(), any(), any());
        verify(contractMilestoneRepository, times(4)).save(any(ContractMilestoneEntity.class));
        assertEquals(ContractMilestoneEntity.STATUS_CANCELLED, deposited.getStatus());
        assertEquals(ContractMilestoneEntity.STATUS_CANCELLED, inProgress.getStatus());
        assertEquals(ContractMilestoneEntity.STATUS_CANCELLED, underReview.getStatus());
        assertEquals(ContractMilestoneEntity.STATUS_CANCELLED, disputed.getStatus());
        assertEquals(ContractEntity.STATUS_TERMINATED, result.getStatus());
        assertEquals("Admin approved closure", result.getTerminationNote());
        assertNotNull(result.getTerminatedAt());
    }

    @Test
    void executeTermination_doesNotRefundApprovedOrCompletedMilestones() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status(ContractEntity.STATUS_ACTIVE).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();
        ContractMilestoneEntity cancelled = ContractMilestoneEntity.builder().contractMilestoneId(101).contractId(1).finalBudget(BigDecimal.valueOf(100)).status(ContractMilestoneEntity.STATUS_CANCELLED).build();
        ContractMilestoneEntity completed = ContractMilestoneEntity.builder().contractMilestoneId(102).contractId(1).finalBudget(BigDecimal.valueOf(200)).status(ContractMilestoneEntity.STATUS_COMPLETED).build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build());
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cancelled, completed));
        when(contractRepository.save(any(ContractEntity.class))).thenAnswer(i -> i.getArgument(0));

        ContractEntity result = contractExecutionService.executeTermination(1, "Completed work preserved");

        verify(walletLedgerService, never()).releaseEscrowToAvailable(any(), any(), any(), any(), any(), any());
        verify(contractMilestoneRepository, never()).save(any(ContractMilestoneEntity.class));
        assertEquals(ContractMilestoneEntity.STATUS_CANCELLED, cancelled.getStatus());
        assertEquals(ContractMilestoneEntity.STATUS_COMPLETED, completed.getStatus());
        assertEquals(ContractEntity.STATUS_TERMINATED, result.getStatus());
    }

    @Test
    void executeTermination_nonAdminCannotExecute() {
        doThrow(new AppException("FORBIDDEN")).when(accessService).requireRole("ADMIN");

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.executeTermination(1, "Should be blocked"));

        assertEquals("FORBIDDEN", ex.getMessage());
        verify(accessService).requireRole("ADMIN");
        verify(contractRepository, never()).findById(any());
        verify(walletLedgerService, never()).releaseEscrowToAvailable(any(), any(), any(), any(), any(), any());
    }

    @Test
    void startMilestone_shouldMoveDepositedToInProgress() {
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(1).status(ContractMilestoneEntity.STATUS_DEPOSITED).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status(ContractEntity.STATUS_ACTIVE).build();
        ContractMilestoneEntity contractMilestone = ContractMilestoneEntity.builder().contractId(1).jobMilestoneId(200).status(ContractMilestoneEntity.STATUS_DEPOSITED).build();
        AccountEntity expert = AccountEntity.builder().accountId(90).role(RoleEntity.builder().roleName("EXPERT").build()).build();

        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(expert);
        when(expertProfileRepository.findByAccountId(90)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        mockLockedContractMilestone(1, 200, contractMilestone);
        when(contractMilestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        MilestoneEntity result = contractExecutionService.startMilestone(200);

        assertEquals(ContractMilestoneEntity.STATUS_IN_PROGRESS, result.getStatus());
        assertEquals(ContractMilestoneEntity.STATUS_IN_PROGRESS, contractMilestone.getStatus());
        verify(auditLogService).record("MILESTONE_STARTED", "milestones", "200", 90);
    }

    @Test
    void startMilestone_shouldReturnAlreadyInProgressMilestone() {
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(1).status(ContractMilestoneEntity.STATUS_IN_PROGRESS).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status(ContractEntity.STATUS_ACTIVE).build();
        ContractMilestoneEntity contractMilestone = ContractMilestoneEntity.builder().contractId(1).jobMilestoneId(200).status(ContractMilestoneEntity.STATUS_IN_PROGRESS).build();
        AccountEntity expert = AccountEntity.builder().accountId(90).role(RoleEntity.builder().roleName("EXPERT").build()).build();

        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(expert);
        when(expertProfileRepository.findByAccountId(90)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        mockLockedContractMilestone(1, 200, contractMilestone);

        MilestoneEntity result = contractExecutionService.startMilestone(200);

        assertEquals(ContractMilestoneEntity.STATUS_IN_PROGRESS, result.getStatus());
        verify(milestoneRepository, never()).save(any());
        verify(auditLogService, never()).record(eq("MILESTONE_STARTED"), any(), any(), any());
    }

    @Test
    void cancelDispute_byInitiatorRestoresPreviousStatus() {
        DisputeEntity dispute = DisputeEntity.builder()
                .disputeId(1)
                .contractId(1)
                .milestoneId(200)
                .initiatedBy("BUSINESS")
                .previousMilestoneStatus(ContractMilestoneEntity.STATUS_UNDER_REVIEW)
                .status(DisputeEntity.STATUS_ESCALATION_REQUESTED)
                .build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).status(ContractMilestoneEntity.STATUS_DISPUTED).build();
        ContractMilestoneEntity contractMilestone = ContractMilestoneEntity.builder().contractId(1).jobMilestoneId(200).status(ContractMilestoneEntity.STATUS_DISPUTED).build();
        AccountEntity business = AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).build();

        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(accessService.currentAccount()).thenReturn(business);
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(expertProfileRepository.findByAccountId(50)).thenReturn(Optional.empty());
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(contractMilestone));
        when(disputeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        DisputeEntity result = contractExecutionService.cancelDispute(1, "Self resolved");

        assertEquals(DisputeEntity.STATUS_CANCELLED, result.getStatus());
        assertEquals(DisputeEntity.RESOLUTION_CANCELLED_BY_INITIATOR, result.getResolutionType());
        assertEquals(ContractMilestoneEntity.STATUS_UNDER_REVIEW, milestone.getStatus());
        assertEquals(ContractMilestoneEntity.STATUS_UNDER_REVIEW, contractMilestone.getStatus());
    }

    @Test
    void cancelDispute_adminCannotCancelDuringStaffReview() {
        DisputeEntity dispute = DisputeEntity.builder()
                .disputeId(1)
                .contractId(1)
                .milestoneId(200)
                .initiatedBy("BUSINESS")
                .previousMilestoneStatus(ContractMilestoneEntity.STATUS_UNDER_REVIEW)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).build();

        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build()
        );
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.cancelDispute(1, "Admin override"));

        assertEquals("ONLY_DISPUTE_INITIATOR_CAN_WITHDRAW", ex.getMessage());
        verify(disputeRepository, never()).save(any());
    }

    @Test
    void executeTerminationSettlement_shouldRequirePartialEvidenceWhenRequested() {
        TerminationRequestEntity request = TerminationRequestEntity.builder()
                .terminationRequestId(9L)
                .contractId(1)
                .currentMilestoneId(200)
                .assignedStaffId(8)
                .status(TerminationRequestEntity.STATUS_AWAITING_SETTLEMENT_EXECUTION)
                .expertPayoutPercentage(BigDecimal.valueOf(25))
                .partialEvidenceRequired(true)
                .build();

        when(terminationRequestRepository.findById(9L)).thenReturn(Optional.of(request));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.executeTerminationSettlement(9L));

        assertEquals("CAN NOP PARTIAL EVIDENCE TRUOC KHI SETTLEMENT", ex.getMessage());
        verify(walletLedgerService, never()).debitEscrow(any(), any(), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class));
    }

    @Test
    void executeTerminationSettlement_shouldSplitEscrowAndAwaitDepositRefund() {
        TerminationRequestEntity request = TerminationRequestEntity.builder()
                .terminationRequestId(9L)
                .contractId(1)
                .currentMilestoneId(200)
                .assignedStaffId(8)
                .status(TerminationRequestEntity.STATUS_AWAITING_SETTLEMENT_EXECUTION)
                .expertPayoutPercentage(BigDecimal.valueOf(25))
                .build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status(ContractEntity.STATUS_TERMINATION_PENDING).build();
        ContractMilestoneEntity current = ContractMilestoneEntity.builder().contractId(1).jobMilestoneId(200).status(ContractMilestoneEntity.STATUS_IN_PROGRESS).finalBudget(BigDecimal.valueOf(400)).build();
        ContractMilestoneEntity future = ContractMilestoneEntity.builder().contractId(1).jobMilestoneId(201).status(ContractMilestoneEntity.STATUS_PENDING).finalBudget(BigDecimal.valueOf(100)).build();
        WalletTransactionEntity debit = WalletTransactionEntity.builder().id(77L).build();

        when(accessService.currentAccount()).thenReturn(AccountEntity.builder().accountId(1).role(RoleEntity.builder().roleName("ADMIN").build()).build());
        when(terminationRequestRepository.findById(9L)).thenReturn(Optional.of(request));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).accountId(50).build()));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).accountId(60).build()));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(current, future));
        mockLockedContractMilestone(1, 200, current);
        when(walletLedgerService.debitEscrow(eq(50), eq(BigDecimal.valueOf(400)), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class))).thenReturn(debit);
        when(walletLedgerService.creditAvailable(eq(60), eq(BigDecimal.valueOf(100).setScale(2)), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class))).thenReturn(WalletTransactionEntity.builder().id(78L).build());
        when(walletLedgerService.creditAvailable(eq(50), eq(BigDecimal.valueOf(300).setScale(2)), any(), any(), any(), any(), any(WalletLedgerService.WalletOperationContext.class))).thenReturn(WalletTransactionEntity.builder().id(79L).build());
        when(contractMilestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contractRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(terminationRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TerminationRequestEntity result = contractExecutionService.executeTerminationSettlement(9L);

        assertEquals(TerminationRequestEntity.STATUS_COMPLETED, result.getStatus());
        assertEquals(BigDecimal.valueOf(100).setScale(2), result.getExpertPayoutAmount());
        assertEquals(BigDecimal.valueOf(300).setScale(2), result.getBusinessRefundAmount());
        assertEquals(77L, result.getSettlementWalletTransactionId());
        assertEquals(ContractMilestoneEntity.STATUS_COMPLETED, current.getStatus());
        assertEquals(ContractMilestoneEntity.STATUS_CANCELLED, future.getStatus());
        assertEquals(ContractEntity.STATUS_TERMINATED, contract.getStatus());
        verify(paymentWalletService).autoRefundParticipantDeposits(eq(1), any());
    }

    // --- v2.3 AC-mapped tests ---

    @Test
    void depositMilestoneEscrow_shouldSetInProgressStartedAt() {
        ContractEntity contract = ContractEntity.builder().contractId(1).jobId(2).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).jobId(2).milestoneName("M1").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractMilestoneId(100).contractId(1).jobMilestoneId(200)
                .finalBudget(BigDecimal.valueOf(1000)).status("PENDING")
                .build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(business));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        mockLockedContractMilestone(1, 200, cm);
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        contractExecutionService.depositMilestoneEscrow(1, 200);

        assertNotNull(cm.getInProgressStartedAt());
        assertEquals("IN_PROGRESS", cm.getStatus());
    }

    @Test
    void startMilestone_shouldNotResetInProgressStartedAt() {
        LocalDateTime depositTime = LocalDateTime.of(2026, 7, 1, 10, 0);
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(1).status(ContractMilestoneEntity.STATUS_DEPOSITED).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status(ContractEntity.STATUS_ACTIVE).build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status(ContractMilestoneEntity.STATUS_DEPOSITED)
                .inProgressStartedAt(depositTime).build();
        AccountEntity expert = AccountEntity.builder().accountId(90).role(RoleEntity.builder().roleName("EXPERT").build()).build();

        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(expert);
        when(expertProfileRepository.findByAccountId(90)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        mockLockedContractMilestone(1, 200, cm);
        when(contractMilestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(milestoneRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        contractExecutionService.startMilestone(200);

        assertEquals(depositTime, cm.getInProgressStartedAt());
        assertEquals(ContractMilestoneEntity.STATUS_IN_PROGRESS, cm.getStatus());
    }

    @Test
    void submitProgressReport_shouldBlockWhilePreviousReportPendingBusinessAck() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(1).milestoneName("M1").status("IN_PROGRESS").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusDays(1)).duration(10).durationUnit("DAY").build();
        MilestoneProgressReportEntity pending = MilestoneProgressReportEntity.builder()
                .progressReportId(1L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACK_PENDING).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build());
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findByMilestoneIdOrderByCreatedAtAsc(200)).thenReturn(List.of(pending));
        when(milestoneProgressReportRepository.findFirstByContractIdAndMilestoneIdOrderByCreatedAtDesc(1, 200))
                .thenReturn(Optional.of(pending));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.submitProgressReport(1, 200, "Another report", 70, null));
        assertEquals("PROGRESS_REPORT_ACK_PENDING", ex.getMessage());
        verify(milestoneProgressReportRepository, never()).save(any());
    }

    @Test
    void submitProgressReport_shouldAcceptAfterAcknowledgement() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(1).milestoneName("M1").status("IN_PROGRESS").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusDays(1)).duration(10).durationUnit("DAY").build();
        MilestoneProgressReportEntity acked = MilestoneProgressReportEntity.builder()
                .progressReportId(2L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACKNOWLEDGED).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build());
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findByMilestoneIdOrderByCreatedAtAsc(200)).thenReturn(List.of(acked));
        when(milestoneProgressReportRepository.findFirstByContractIdAndMilestoneIdOrderByCreatedAtDesc(1, 200))
                .thenReturn(Optional.of(acked));
        when(milestoneProgressReportRepository.save(any(MilestoneProgressReportEntity.class))).thenAnswer(i -> i.getArgument(0));

        MilestoneProgressReportEntity result = contractExecutionService.submitProgressReport(1, 200, "Next update", 80, null);

        assertEquals(MilestoneProgressReportEntity.ACK_PENDING, result.getAcknowledgementState());
        assertEquals("Next update", result.getContent());
    }

    @Test
    void requestProgressReport_shouldBlockWhileReportPendingBusinessAck() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusDays(1)).duration(10).durationUnit("DAY").build();
        MilestoneProgressReportEntity pending = MilestoneProgressReportEntity.builder()
                .progressReportId(1L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACK_PENDING).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("BUSINESS").build()).status("Approved").build());
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        mockLockedContractMilestone(1, 200, cm);
        when(milestoneProgressReportRepository.findFirstByContractIdAndMilestoneIdOrderByCreatedAtDesc(1, 200))
                .thenReturn(Optional.of(pending));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.requestProgressReport(1, 200));
        assertEquals("PROGRESS_REPORT_ACK_PENDING", ex.getMessage());
    }

    @Test
    void feedbackProgressReport_shouldStoreFeedbackAcknowledgeAndNotify() {
        MilestoneProgressReportEntity report = MilestoneProgressReportEntity.builder()
                .progressReportId(1L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACK_PENDING).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS").build();
        ProgressReportFeedbackRequest request = new ProgressReportFeedbackRequest();
        request.setCategory("SCOPE");
        request.setSeverity("INFO");
        request.setDodItems(List.of("API_CONTRACT_STABLE"));
        request.setFeedback("  Tien do tot, bo sung demo link o lan sau.  ");
        request.setRequiresAdjustment(true);

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(milestoneProgressReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).accountId(60).build()));

        MilestoneProgressReportEntity result = contractExecutionService.feedbackProgressReport(1, 200, 1L, request);

        assertEquals("Tien do tot, bo sung demo link o lan sau.", result.getBusinessFeedback());
        assertEquals("SCOPE", result.getFeedbackCategory());
        assertEquals("INFO", result.getFeedbackSeverity());
        assertTrue(result.getFeedbackDodItems().contains("API_CONTRACT_STABLE"));
        assertTrue(result.getRequiresAdjustment());
        assertEquals(Integer.valueOf(50), result.getFeedbackByAccountId());
        assertNotNull(result.getFeedbackAt());
        assertEquals(MilestoneProgressReportEntity.ACKNOWLEDGED, result.getAcknowledgementState());
        assertEquals(Integer.valueOf(50), result.getAcknowledgedByAccountId());
        assertNotNull(result.getAcknowledgedAt());
        verify(auditLogService).record(eq("PROGRESS_REPORT_FEEDBACK_RECORDED"), eq("milestone_progress_reports"), eq("1"), eq(50));
        verify(notificationService).notifyProgressReportFeedbackRecorded(60, 50, 1, 200, 1L);
    }

    @Test
    void feedbackProgressReport_shouldRejectBlankFeedback() {
        MilestoneProgressReportEntity report = MilestoneProgressReportEntity.builder()
                .progressReportId(1L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACK_PENDING).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS").build();
        ProgressReportFeedbackRequest request = new ProgressReportFeedbackRequest();
        request.setFeedback(" ");

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findById(1L)).thenReturn(Optional.of(report));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.feedbackProgressReport(1, 200, 1L, request));

        assertEquals("PROGRESS_REPORT_FEEDBACK_NOT_ALLOWED", ex.getMessage());
        verify(milestoneProgressReportRepository, never()).save(any());
    }

    @Test
    void submitProgressReport_shouldAcceptAfterBusinessFeedbackAcknowledgesPreviousReport() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        MilestoneEntity milestone = MilestoneEntity.builder().milestoneId(200).contractId(1).milestoneName("M1").status("IN_PROGRESS").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS")
                .inProgressStartedAt(LocalDateTime.now().minusDays(1)).duration(10).durationUnit("DAY").build();
        MilestoneProgressReportEntity feedbackHandled = MilestoneProgressReportEntity.builder()
                .progressReportId(1L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACKNOWLEDGED)
                .businessFeedback("Can bo sung demo link trong bao cao sau").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build());
        when(expertProfileRepository.findByAccountId(99)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(milestoneRepository.findById(200)).thenReturn(Optional.of(milestone));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findByMilestoneIdOrderByCreatedAtAsc(200)).thenReturn(List.of(feedbackHandled));
        when(milestoneProgressReportRepository.findFirstByContractIdAndMilestoneIdOrderByCreatedAtDesc(1, 200))
                .thenReturn(Optional.of(feedbackHandled));
        when(milestoneProgressReportRepository.save(any(MilestoneProgressReportEntity.class))).thenAnswer(i -> i.getArgument(0));

        MilestoneProgressReportEntity result = contractExecutionService.submitProgressReport(1, 200, "Next report", 90, null);

        assertEquals(MilestoneProgressReportEntity.ACK_PENDING, result.getAcknowledgementState());
        assertEquals("Next report", result.getContent());
    }

    @Test
    void acknowledgeProgressReport_shouldSetAcknowledgedAndNotify() {
        MilestoneProgressReportEntity report = MilestoneProgressReportEntity.builder()
                .progressReportId(1L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACK_PENDING).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(milestoneProgressReportRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).accountId(60).build()));

        MilestoneProgressReportEntity result = contractExecutionService.acknowledgeProgressReport(1, 200, 1L);

        assertEquals(MilestoneProgressReportEntity.ACKNOWLEDGED, result.getAcknowledgementState());
        assertEquals(Integer.valueOf(50), result.getAcknowledgedByAccountId());
        assertNotNull(result.getAcknowledgedAt());
        verify(auditLogService).record(eq("PROGRESS_REPORT_ACKNOWLEDGED"), any(), any(), eq(50));
    }

    @Test
    void acknowledgeProgressReport_shouldRejectAlreadyAcknowledged() {
        MilestoneProgressReportEntity report = MilestoneProgressReportEntity.builder()
                .progressReportId(1L).contractId(1).milestoneId(200)
                .acknowledgementState(MilestoneProgressReportEntity.ACKNOWLEDGED).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status("IN_PROGRESS").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));
        when(milestoneProgressReportRepository.findById(1L)).thenReturn(Optional.of(report));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.acknowledgeProgressReport(1, 200, 1L));
        assertEquals("PROGRESS_REPORT_ACK_NOT_ALLOWED", ex.getMessage());
    }

    @Test
    void acknowledgeProgressReport_shouldRejectOnTerminalMilestone() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status("ACTIVE").build();
        ContractMilestoneEntity cm = ContractMilestoneEntity.builder()
                .contractId(1).jobMilestoneId(200).status(ContractMilestoneEntity.STATUS_COMPLETED).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).status("Approved").build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of(cm));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.acknowledgeProgressReport(1, 200, 1L));
        assertEquals("PROGRESS_REPORT_ACK_NOT_ALLOWED", ex.getMessage());
    }

    @Test
    void cancelDraftContract_shouldCancelUntouchedDraftAndReturnJobToOpen() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .status(ContractEntity.STATUS_DRAFT)
                .businessAcceptedAt(null).expertAcceptedAt(null)
                .businessNdaSignedAt(null).expertNdaSignedAt(null)
                .build();
        JobEntity job = JobEntity.builder().jobId(2).status("IN_PROGRESS").build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(jobRepository.findById(2)).thenReturn(Optional.of(job));
        when(contractRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).accountId(60).build()));

        ContractEntity result = contractExecutionService.cancelDraftContract(1);

        assertEquals(ContractEntity.STATUS_CANCELLED, result.getStatus());
        assertEquals("OPEN", job.getStatus());
        verify(auditLogService).record(eq("CONTRACT_DRAFT_CANCELLED_BY_BUSINESS"), eq("contracts"), eq("1"), eq(50));
    }

    @Test
    void cancelDraftContract_shouldRejectAfterSignature() {
        ContractEntity contract = ContractEntity.builder()
                .contractId(1).jobId(2).businessId(10).expertId(5)
                .status(ContractEntity.STATUS_PENDING)
                .businessAcceptedAt(LocalDateTime.now())
                .businessNdaSignedAt(null).expertAcceptedAt(null).expertNdaSignedAt(null)
                .build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.cancelDraftContract(1));
        assertEquals("CONTRACT_DRAFT_CANCELLATION_NOT_ALLOWED", ex.getMessage());
    }

    @Test
    void routeDispute_shouldRequireStaffRole() {
        doThrow(new AppException("FORBIDDEN")).when(accessService).requireRole("STAFF");

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.routeDispute(1, 9));
        assertEquals("FORBIDDEN", ex.getMessage());
    }

    @Test
    void routeDispute_shouldAutoPickStaffWhenNull() {
        DisputeEntity dispute = DisputeEntity.builder().disputeId(1).contractId(1).milestoneId(200).status(DisputeEntity.STATUS_ESCALATION_REQUESTED).build();
        StaffEntity staff = StaffEntity.builder().staffId(9).accountId(5).build();
        DomainEntity domain = DomainEntity.builder().domainId(2).domainCode("GEN_AI").domainName("Gen AI").isActive(true).sortOrder(1).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(3).role(RoleEntity.builder().roleName("STAFF").build()).build());
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(disputeRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(staffRepository.findById(9)).thenReturn(Optional.of(staff));
        when(staffRepository.findAllForDisputeRouting()).thenReturn(List.of(staff));
        when(contractRepository.findById(1)).thenReturn(Optional.of(ContractEntity.builder().contractId(1).businessId(10).expertId(5).jobId(50).build()));
        when(jobDomainRepository.findByIdJobId(50)).thenReturn(List.of(
                new JobDomainEntity(new JobDomainId(50, 2), LocalDateTime.now())));
        when(jobSkillRepository.findByIdJobId(50)).thenReturn(List.of());
        when(staffDomainRepository.findByIdStaffId(9)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(9, 2))));
        when(staffSkillRepository.findByIdStaffId(9)).thenReturn(List.of());
        when(domainRepository.findAllById(List.of(2))).thenReturn(List.of(domain));
        when(skillRepository.findAllById(any())).thenReturn(List.of());
        when(disputeRepository.findByAssignedStaffId(any())).thenReturn(List.of());
        when(accountRepository.findById(5)).thenReturn(Optional.of(AccountEntity.builder().accountId(5).fullName("Staff")
                .role(RoleEntity.builder().roleName("STAFF").build()).status("Approved").build()));

        DisputeEntity result = contractExecutionService.routeDispute(1, null);

        assertEquals(DisputeEntity.STATUS_STAFF_REVIEWING, result.getStatus());
        assertEquals(Integer.valueOf(9), result.getAssignedStaffId());
        verify(auditLogService).record(eq("DISPUTE_STAFF_AUTO_ASSIGNED"), eq("disputes"), eq("1"), eq(3));
    }

    @Test
    void listStaffCandidates_shouldRequireStaffRole() {
        doThrow(new AppException("FORBIDDEN")).when(accessService).requireRole("STAFF");

        AppException ex = assertThrows(AppException.class,
                () -> contractExecutionService.listStaffCandidates(1));
        assertEquals("FORBIDDEN", ex.getMessage());
    }

    @Test
    void acceptBusinessTermination_shouldAutoRefundParticipantDeposits() {
        TerminationRequestEntity request = TerminationRequestEntity.builder()
                .terminationRequestId(1L).contractId(1).status(TerminationRequestEntity.STATUS_AWAITING_EXPERT_RESPONSE).build();
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5).status(ContractEntity.STATUS_ACTIVE).build();

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(60).role(RoleEntity.builder().roleName("EXPERT").build()).status("Approved").build());
        when(expertProfileRepository.findByAccountId(60)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).build()));
        when(terminationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).accountId(50).build()));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(ExpertProfileEntity.builder().expertId(5).accountId(60).build()));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(contractRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(terminationRequestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TerminationRequestEntity result = contractExecutionService.acceptBusinessTermination(1L);

        verify(paymentWalletService).autoRefundParticipantDeposits(eq(1), eq(60));
        assertNotNull(result.getDepositRefundedAt());
    }

    @Test
    void executeImmediateTermination_shouldAutoRefundDepositsAndCloseContract() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).expertId(5)
                .totalBudget(BigDecimal.valueOf(100_000_000)).status(ContractEntity.STATUS_ACTIVE).build();
        BusinessProfileEntity business = BusinessProfileEntity.builder().businessId(10).accountId(50).build();
        ExpertProfileEntity expert = ExpertProfileEntity.builder().expertId(5).accountId(60).build();
        ImmediateTerminationRequest input = new ImmediateTerminationRequest();
        input.setReason("Late on reports");
        input.setConfirmedPenalty(true);

        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(50).role(RoleEntity.builder().roleName("BUSINESS").build()).status("Approved").build());
        when(businessProfileRepository.findByAccountId(50)).thenReturn(Optional.of(business));
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(businessProfileRepository.findById(10)).thenReturn(Optional.of(business));
        when(expertProfileRepository.findById(5)).thenReturn(Optional.of(expert));
        when(terminationRequestRepository.findByContractIdAndStatusIn(eq(1), any())).thenReturn(List.of());
        when(disputeRepository.findByContractId(eq(1))).thenReturn(List.of());
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(1)).thenReturn(List.of());
        when(paymentWalletService.immediateTerminateContract(any(), any(), any())).thenReturn(contract);

        contractExecutionService.immediateTerminate(1, input);

        verify(paymentWalletService).immediateTerminateContract(any(), any(), any());
        verify(auditLogService).record(eq("CONTRACT_IMMEDIATE_TERMINATED"), eq("contracts"), eq("1"), eq(50));
    }
}
