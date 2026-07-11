package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.core.StaffAssignmentCandidateResponse;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StaffDisputeInboxRoutingTest {

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

    private AccountEntity staffActor;
    private StaffEntity staff1;
    private StaffEntity staff2;
    private DisputeEntity dispute;
    private ContractEntity contract;

    @BeforeEach
    void setUp() {
        staffActor = AccountEntity.builder()
                .accountId(10).email("staff1@test.com").fullName("Staff One")
                .role(RoleEntity.builder().roleId(4).roleName("STAFF").build())
                .status("Approved").build();

        staff1 = StaffEntity.builder().staffId(1).accountId(10)
                .specialization("AI Specialist").build();
        staff2 = StaffEntity.builder().staffId(2).accountId(11)
                .specialization("Web Dev").build();

        contract = ContractEntity.builder().contractId(100).jobId(50)
                .businessId(1).expertId(2).build();

        dispute = DisputeEntity.builder()
                .disputeId(1).contractId(100).milestoneId(10)
                .status(DisputeEntity.STATUS_ESCALATION_REQUESTED)
                .createdAt(LocalDateTime.now()).build();

        lenient().when(accessService.currentAccount()).thenReturn(staffActor);
    }

    @Test
    void listStaffCandidates_deterministicOrdering() {
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(contractRepository.findById(100)).thenReturn(Optional.of(contract));

        DomainEntity domainGenAi = DomainEntity.builder().domainId(2).domainCode("GEN_AI")
                .domainName("Generative AI").isActive(true).sortOrder(1).build();
        DomainEntity domainNlp = DomainEntity.builder().domainId(3).domainCode("NLP")
                .domainName("NLP").isActive(true).sortOrder(2).build();
        SkillEntity skillPrompt = SkillEntity.builder().skillId(1).skillCode("PROMPT")
                .skillName("Prompt Engineering").isActive(true).build();

        when(jobDomainRepository.findByIdJobId(50)).thenReturn(List.of(
                new JobDomainEntity(new JobDomainId(50, 2), LocalDateTime.now()),
                new JobDomainEntity(new JobDomainId(50, 3), LocalDateTime.now())));
        when(jobSkillRepository.findByIdJobId(50)).thenReturn(List.of(
                JobSkillEntity.builder().id(new JobSkillId(50, 1)).isMandatory(true).build()));

        when(staffRepository.findAll()).thenReturn(List.of(staff1, staff2));
        when(staffDomainRepository.findByIdStaffId(1)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(1, 2)),
                new StaffDomainEntity(new StaffDomainId(1, 3))));
        when(staffDomainRepository.findByIdStaffId(2)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(2, 2))));
        when(staffSkillRepository.findByIdStaffId(1)).thenReturn(List.of(
                new StaffSkillEntity(new StaffSkillId(1, 1))));
        when(staffSkillRepository.findByIdStaffId(2)).thenReturn(List.of());

        when(domainRepository.findAllById(List.of(2, 3))).thenReturn(List.of(domainGenAi, domainNlp));
        when(domainRepository.findAllById(List.of(2))).thenReturn(List.of(domainGenAi));
        when(skillRepository.findAllById(any())).thenReturn(List.of(skillPrompt));

        when(disputeRepository.findByAssignedStaffId(anyInt())).thenReturn(List.of());
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffActor));

        AccountEntity staffBAccount = AccountEntity.builder().accountId(11).fullName("Staff Two")
                .role(RoleEntity.builder().roleId(4).roleName("STAFF").build()).build();
        when(accountRepository.findById(11)).thenReturn(Optional.of(staffBAccount));

        List<StaffAssignmentCandidateResponse> candidates = contractService.listStaffCandidates(1);

        assertFalse(candidates.isEmpty());
        assertEquals(1, candidates.get(0).getStaffId());
        assertEquals(2, candidates.get(0).getMatchedDomains().size());
        assertEquals(1, candidates.get(1).getMatchedDomains().size());
    }

    @Test
    void listStaffCandidates_excludesIneligibleStaff() {
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(contractRepository.findById(100)).thenReturn(Optional.of(contract));

        DomainEntity domainGenAi = DomainEntity.builder().domainId(2).domainCode("GEN_AI")
                .domainName("Generative AI").isActive(true).sortOrder(1).build();
        DomainEntity domainWeb = DomainEntity.builder().domainId(10).domainCode("WEB")
                .domainName("Web Development").isActive(true).sortOrder(2).build();

        when(jobDomainRepository.findByIdJobId(50)).thenReturn(List.of(
                new JobDomainEntity(new JobDomainId(50, 2), LocalDateTime.now())));
        when(jobSkillRepository.findByIdJobId(50)).thenReturn(List.of());

        when(staffRepository.findAll()).thenReturn(List.of(staff1, staff2));
        when(staffDomainRepository.findByIdStaffId(1)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(1, 2))));
        when(staffDomainRepository.findByIdStaffId(2)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(2, 10))));

        when(domainRepository.findAllById(List.of(2))).thenReturn(List.of(domainGenAi));
        when(domainRepository.findAllById(List.of(10))).thenReturn(List.of(domainWeb));
        when(skillRepository.findAllById(any())).thenReturn(List.of());
        when(staffSkillRepository.findByIdStaffId(anyInt())).thenReturn(List.of());
        when(disputeRepository.findByAssignedStaffId(anyInt())).thenReturn(List.of());
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffActor));

        List<StaffAssignmentCandidateResponse> candidates = contractService.listStaffCandidates(1);

        assertEquals(1, candidates.size());
        assertEquals(1, candidates.get(0).getStaffId());
        assertFalse(candidates.get(0).getMatchedDomains().isEmpty());
    }

    @Test
    void routeDispute_manualDomainMismatch_throwsException() {
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(contractRepository.findById(100)).thenReturn(Optional.of(contract));
        when(jobDomainRepository.findByIdJobId(50)).thenReturn(List.of(
                new JobDomainEntity(new JobDomainId(50, 2), LocalDateTime.now())));
        when(staffDomainRepository.findByIdStaffId(2)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(2, 10))));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(anyInt())).thenReturn(List.of());

        assertThrows(AppException.class, () -> contractService.routeDispute(1, 2));
    }

    @Test
    void routeDispute_noMatchingStaff_throwsException() {
        when(disputeRepository.findById(1)).thenReturn(Optional.of(dispute));
        when(contractRepository.findById(100)).thenReturn(Optional.of(contract));
        when(jobDomainRepository.findByIdJobId(50)).thenReturn(List.of(
                new JobDomainEntity(new JobDomainId(50, 2), LocalDateTime.now())));
        when(jobSkillRepository.findByIdJobId(50)).thenReturn(List.of());
        when(staffRepository.findAll()).thenReturn(List.of(staff2));
        when(staffDomainRepository.findByIdStaffId(2)).thenReturn(List.of(
                new StaffDomainEntity(new StaffDomainId(2, 10))));

        DomainEntity domainGenAi = DomainEntity.builder().domainId(2).domainCode("GEN_AI")
                .domainName("Generative AI").isActive(true).sortOrder(1).build();
        when(domainRepository.findAllById(List.of(2))).thenReturn(List.of(domainGenAi));
        when(domainRepository.findAllById(List.of(10))).thenReturn(List.of());
        when(skillRepository.findAllById(any())).thenReturn(List.of());
        when(staffSkillRepository.findByIdStaffId(anyInt())).thenReturn(List.of());
        when(disputeRepository.findByAssignedStaffId(anyInt())).thenReturn(List.of());
        when(accountRepository.findById(11)).thenReturn(Optional.of(
                AccountEntity.builder().accountId(11).fullName("Staff Two").build()));

        AppException ex = assertThrows(AppException.class,
                () -> contractService.routeDispute(1, null));
        assertTrue(ex.getMessage().contains("NO_MATCHING_STAFF_FOR_JOB_DOMAIN"));
    }
}
