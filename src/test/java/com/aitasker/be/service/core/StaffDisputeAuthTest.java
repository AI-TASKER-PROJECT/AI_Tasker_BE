package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StaffDisputeAuthTest {

    @Mock private AccessService accessService;
    @Mock private AccountRepository accountRepository;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private ProposalRepository proposalRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private ContractMilestoneRepository contractMilestoneRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private AcceptanceCriteriaRepository criteriaRepository;
    @Mock private DeliverableRepository deliverableRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private DisputeRepository disputeRepository;
    @Mock private TerminationRequestRepository terminationRequestRepository;
    @Mock private CaseAttachmentRepository caseAttachmentRepository;
    @Mock private MilestoneProgressReportRepository milestoneProgressReportRepository;
    @Mock private MilestoneProgressReportRequestRepository milestoneProgressReportRequestRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private SystemSettingRepository systemSettingRepository;
    @Mock private SystemWalletService systemWalletService;
    @Mock private WalletLedgerService walletLedgerService;
    @Mock private PaymentWalletService paymentWalletService;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private JobDomainRepository jobDomainRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private StaffDomainRepository staffDomainRepository;
    @Mock private StaffSkillRepository staffSkillRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private SkillRepository skillRepository;

    @InjectMocks private ContractExecutionService contractService;

    private AccountEntity staffA;
    private StaffEntity staffProfileA;
    private DisputeEntity disputeOfA;
    private ContractEntity contract;

    @BeforeEach
    void setUp() {
        staffA = AccountEntity.builder()
                .accountId(10).email("staffA@test.com").fullName("Staff A")
                .role(RoleEntity.builder().roleId(4).roleName("STAFF").build())
                .status("Approved").build();

        staffProfileA = StaffEntity.builder().staffId(1).accountId(10).build();

        contract = ContractEntity.builder().contractId(100).jobId(50)
                .businessId(1).expertId(2).build();

        disputeOfA = DisputeEntity.builder()
                .disputeId(1).contractId(100).milestoneId(10)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .assignedStaffId(1).build();

        lenient().when(accessService.currentAccount()).thenReturn(staffA);
        lenient().when(staffRepository.findByAccountId(10)).thenReturn(Optional.of(staffProfileA));
    }

    @Test
    void getDispute_staffCanReadAssignedDispute() {
        when(disputeRepository.findById(1)).thenReturn(Optional.of(disputeOfA));

        DisputeEntity result = contractService.getDispute(1);

        assertNotNull(result);
        assertEquals(1, result.getDisputeId());
    }

    @Test
    void getDispute_staffCannotReadOtherStaffDispute() {
        DisputeEntity otherDispute = DisputeEntity.builder()
                .disputeId(2).contractId(101).milestoneId(11)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .assignedStaffId(2).build();

        when(disputeRepository.findById(2)).thenReturn(Optional.of(otherDispute));

        assertThrows(AppException.class, () -> contractService.getDispute(2));
    }

    @Test
    void listDisputesByContract_staffSeesOnlyAssigned() {
        DisputeEntity otherStaffDispute = DisputeEntity.builder()
                .disputeId(2).contractId(100).milestoneId(11)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .assignedStaffId(2).build();

        when(contractRepository.findById(100)).thenReturn(Optional.of(contract));
        when(disputeRepository.findByContractId(100)).thenReturn(
                List.of(disputeOfA, otherStaffDispute));

        List<DisputeEntity> result = contractService.listDisputesByContract(100);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getDisputeId());
    }

    // ── Attachment authorization tests ──

    @Test
    void listCaseAttachments_staffCanReadOwnDisputeAttachments() {
        when(disputeRepository.findById(1)).thenReturn(Optional.of(disputeOfA));
        CaseAttachmentEntity attachment = CaseAttachmentEntity.builder()
                .attachmentId(1L).ownerType("DISPUTE").ownerId(1L)
                .fileUrl("/files/evidence.pdf").uploadedByAccountId(99).build();
        when(caseAttachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc("DISPUTE", 1L))
                .thenReturn(List.of(attachment));

        List<CaseAttachmentEntity> result = contractService.listCaseAttachments("DISPUTE", 1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void listCaseAttachments_staffCannotReadOtherStaffDisputeAttachments() {
        DisputeEntity disputeOfB = DisputeEntity.builder()
                .disputeId(2).contractId(100).milestoneId(11)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .assignedStaffId(2).build();

        when(disputeRepository.findById(2)).thenReturn(Optional.of(disputeOfB));

        assertThrows(AppException.class,
                () -> contractService.listCaseAttachments("DISPUTE", 2L));
    }

    @Test
    void createCaseAttachment_staffCannotCreateForOtherStaffDispute() {
        DisputeEntity disputeOfB = DisputeEntity.builder()
                .disputeId(2).contractId(100).milestoneId(11)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .assignedStaffId(2).build();

        when(disputeRepository.findById(2)).thenReturn(Optional.of(disputeOfB));

        CaseAttachmentEntity input = CaseAttachmentEntity.builder()
                .ownerType("DISPUTE").ownerId(2L)
                .fileUrl("/files/malicious.pdf").build();

        assertThrows(AppException.class,
                () -> contractService.createCaseAttachment(input));
        verify(caseAttachmentRepository, never()).save(any());
    }

    @Test
    void listCaseAttachments_adminCanReadAnyDisputeAttachments() {
        DisputeEntity disputeOfB = DisputeEntity.builder()
                .disputeId(2).contractId(100).milestoneId(11)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .assignedStaffId(2).build();

        AccountEntity admin = AccountEntity.builder()
                .accountId(1).email("admin@test.com").fullName("Admin")
                .role(RoleEntity.builder().roleId(3).roleName("ADMIN").build())
                .status("Approved").build();

        when(accessService.currentAccount()).thenReturn(admin);
        when(disputeRepository.findById(2)).thenReturn(Optional.of(disputeOfB));
        when(contractRepository.findById(100)).thenReturn(Optional.of(contract));
        when(caseAttachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc("DISPUTE", 2L))
                .thenReturn(List.of());

        List<CaseAttachmentEntity> result = contractService.listCaseAttachments("DISPUTE", 2L);

        assertNotNull(result);
    }
}
