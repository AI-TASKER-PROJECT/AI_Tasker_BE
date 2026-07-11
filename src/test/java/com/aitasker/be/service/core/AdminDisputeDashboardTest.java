package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.common.exception.NotFoundException;
import com.aitasker.be.dto.admin.*;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDisputeDashboardTest {

    @Mock private AccessService accessService;
    @Mock private DisputeRepository disputeRepository;
    @Mock private WalletTransactionRepository walletTransactionRepository;
    @Mock private CaseAttachmentRepository caseAttachmentRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private ContractMilestoneRepository contractMilestoneRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private StaffRepository staffRepository;

    @InjectMocks private AdminDisputeDashboardService service;

    private DisputeEntity resolvedDispute;
    private DisputeEntity unsettledDispute;
    private StaffEntity staff;
    private AccountEntity staffAccount;

    @BeforeEach
    void setUp() {
        staffAccount = AccountEntity.builder()
                .accountId(10)
                .fullName("Nguyen Van A")
                .build();

        staff = StaffEntity.builder()
                .staffId(12)
                .accountId(10)
                .build();

        resolvedDispute = DisputeEntity.builder()
                .disputeId(8)
                .contractId(42)
                .milestoneId(103)
                .status(DisputeEntity.STATUS_RESOLVED)
                .initiatedBy("BUSINESS")
                .initiationType(DisputeEntity.INITIATION_BUSINESS_REJECTED_DELIVERABLE)
                .createdAt(LocalDateTime.of(2026, 7, 11, 10, 0))
                .assignedStaffId(12)
                .staffDecidedAt(LocalDateTime.of(2026, 7, 12, 9, 30))
                .staffDecisionPercentage(70)
                .staffProposedExpertAmount(new BigDecimal("7000000"))
                .businessRefundAmount(new BigDecimal("3000000"))
                .settlementExecutedAt(LocalDateTime.of(2026, 7, 12, 9, 30, 1))
                .settlementWalletTransactionId(1234L)
                .build();

        unsettledDispute = DisputeEntity.builder()
                .disputeId(9)
                .contractId(43)
                .milestoneId(104)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .initiatedBy("EXPERT")
                .initiationType(DisputeEntity.INITIATION_EXPERT_SCOPE_CONCERN)
                .createdAt(LocalDateTime.of(2026, 7, 10, 8, 0))
                .build();
    }

    @Test
    void listDisputes_shouldReturnPaginatedList() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute, unsettledDispute));
        when(staffRepository.findById(12)).thenReturn(Optional.of(staff));
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffAccount));

        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setPage(0);
        filter.setSize(20);

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(2, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void listDisputes_shouldFilterByStatus() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute, unsettledDispute));
        when(staffRepository.findById(12)).thenReturn(Optional.of(staff));
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffAccount));

        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setStatus(DisputeEntity.STATUS_RESOLVED);

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals(8, result.getContent().get(0).getDisputeId());
    }

    @Test
    void listDisputes_shouldFilterByAssignedStaffId() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute, unsettledDispute));
        when(staffRepository.findById(12)).thenReturn(Optional.of(staff));
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffAccount));

        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setAssignedStaffId(12);

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals(12, result.getContent().get(0).getAssignedStaff().getStaffId());
    }

    @Test
    void listDisputes_shouldFilterByDateRange() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute, unsettledDispute));
        when(staffRepository.findById(12)).thenReturn(Optional.of(staff));
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffAccount));

        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setFrom(LocalDateTime.of(2026, 7, 11, 0, 0));
        filter.setTo(LocalDateTime.of(2026, 7, 12, 0, 0));

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals(8, result.getContent().get(0).getDisputeId());
    }

    @Test
    void listDisputes_shouldSearchByQ() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute, unsettledDispute));
        when(staffRepository.findById(12)).thenReturn(Optional.of(staff));
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffAccount));

        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setQ("42");

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals(42, result.getContent().get(0).getContractId());
    }

    @Test
    void listDisputes_shouldRejectInvalidStatus() {
        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setStatus("INVALID_STATUS");

        AppException ex = assertThrows(AppException.class, () -> service.listDisputes(filter));
        assertEquals("DISPUTE STATUS KHONG HOP LE", ex.getMessage());
    }

    @Test
    void listDisputes_shouldRejectFromAfterTo() {
        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setFrom(LocalDateTime.of(2026, 7, 12, 0, 0));
        filter.setTo(LocalDateTime.of(2026, 7, 11, 0, 0));

        AppException ex = assertThrows(AppException.class, () -> service.listDisputes(filter));
        assertEquals("FROM KHONG DUOC LON HON TO", ex.getMessage());
    }

    @Test
    void listDisputes_shouldRejectQLongerThan100() {
        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setQ("a".repeat(101));

        AppException ex = assertThrows(AppException.class, () -> service.listDisputes(filter));
        assertEquals("Q KHONG DUOC VUOT QUA 100 KY TU", ex.getMessage());
    }

    @Test
    void listDisputes_shouldRejectNegativePage() {
        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setPage(-1);

        AppException ex = assertThrows(AppException.class, () -> service.listDisputes(filter));
        assertEquals("PAGE KHONG DUOC AM", ex.getMessage());
    }

    @Test
    void listDisputes_shouldRejectSizeOutOfBounds() {
        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setSize(200);

        AppException ex = assertThrows(AppException.class, () -> service.listDisputes(filter));
        assertEquals("SIZE PHAI NAM TRONG KHOANG 1 DEN 100", ex.getMessage());
    }

    @Test
    void listDisputes_shouldRejectSizeZero() {
        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setSize(0);

        AppException ex = assertThrows(AppException.class, () -> service.listDisputes(filter));
        assertEquals("SIZE PHAI NAM TRONG KHOANG 1 DEN 100", ex.getMessage());
    }

    @Test
    void listDisputes_shouldPaginateCorrectly() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute, unsettledDispute));

        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setPage(0);
        filter.setSize(1);

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(8, result.getContent().get(0).getDisputeId());
    }

    @Test
    void listDisputes_shouldReturnEmptyContentForPageBeyondTotal() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute));

        AdminDisputeFilter filter = new AdminDisputeFilter();
        filter.setPage(5);
        filter.setSize(20);

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(1, result.getTotalElements());
        assertEquals(0, result.getContent().size());
    }

    @Test
    void listDisputes_shouldUseDefaultPageAndSize() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute));

        AdminDisputeFilter filter = new AdminDisputeFilter();

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
    }

    @Test
    void listDisputes_unsettledDisputeShouldHaveNullFinancials() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(unsettledDispute));

        AdminDisputeFilter filter = new AdminDisputeFilter();

        AdminDisputeListResponse result = service.listDisputes(filter);

        assertEquals(1, result.getContent().size());
        AdminDisputeListItem item = result.getContent().get(0);
        assertNull(item.getExpertPayoutPercentage());
        assertNull(item.getExpertPayoutAmount());
        assertNull(item.getBusinessRefundAmount());
        assertNull(item.getSettlementExecutedAt());
        assertNull(item.getSettlementWalletTransactionId());
    }

    @Test
    void listDisputes_settledDisputeShouldHaveFinancials() {
        when(disputeRepository.findAllByOrderByCreatedAtDesc())
                .thenReturn(List.of(resolvedDispute));
        when(staffRepository.findById(12)).thenReturn(Optional.of(staff));
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffAccount));

        AdminDisputeFilter filter = new AdminDisputeFilter();

        AdminDisputeListResponse result = service.listDisputes(filter);

        AdminDisputeListItem item = result.getContent().get(0);
        assertEquals(70, item.getExpertPayoutPercentage());
        assertEquals(new BigDecimal("7000000"), item.getExpertPayoutAmount());
        assertEquals(new BigDecimal("3000000"), item.getBusinessRefundAmount());
        assertEquals(LocalDateTime.of(2026, 7, 12, 9, 30, 1), item.getSettlementExecutedAt());
        assertEquals(1234L, item.getSettlementWalletTransactionId());
    }

    @Test
    void getDisputeDetail_shouldReturnSettledDisputeWithLedgerAndAttachments() {
        when(disputeRepository.findById(8)).thenReturn(Optional.of(resolvedDispute));
        when(staffRepository.findById(12)).thenReturn(Optional.of(staff));
        when(accountRepository.findById(10)).thenReturn(Optional.of(staffAccount));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(42))
                .thenReturn(List.of(ContractMilestoneEntity.builder()
                        .contractMilestoneId(1)
                        .contractId(42)
                        .jobMilestoneId(103)
                        .finalBudget(new BigDecimal("10000000"))
                        .settlementSourceType("DISPUTE")
                        .settlementSourceId(8L)
                        .build()));
        when(caseAttachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc("DISPUTE", 8L))
                .thenReturn(List.of(CaseAttachmentEntity.builder()
                        .attachmentId(1L)
                        .fileName("evidence.pdf")
                        .fileUrl("/files/ev.pdf")
                        .fileType("application/pdf")
                        .note("dispute evidence")
                        .build()));
        when(walletTransactionRepository.findByReferenceTypeAndReferenceIdOrderByCreatedAtAsc("DISPUTE", 8L))
                .thenReturn(List.of(WalletTransactionEntity.builder()
                        .id(1234L)
                        .transactionType(WalletTransactionEntity.TX_ESCROW_SETTLEMENT_PAYOUT)
                        .direction("DEBIT")
                        .balanceType("ESCROW")
                        .amount(new BigDecimal("7000000"))
                        .description("Dispute settlement debit")
                        .build()));

        AdminDisputeDetail result = service.getDisputeDetail(8);

        assertEquals(8, result.getDisputeId());
        assertEquals(70, result.getExpertPayoutPercentage());
        assertEquals(new BigDecimal("7000000"), result.getExpertPayoutAmount());
        assertEquals(new BigDecimal("3000000"), result.getBusinessRefundAmount());
        assertEquals("Nguyen Van A", result.getAssignedStaff().getDisplayName());
        assertEquals(new BigDecimal("10000000"), result.getMilestoneEscrowAmount());
        assertEquals("DISPUTE", result.getSettlementSourceType());
        assertEquals(1, result.getAttachments().size());
        assertEquals("evidence.pdf", result.getAttachments().get(0).getFileName());
        assertEquals(1, result.getSettlementLedger().size());
    }

    @Test
    void getDisputeDetail_shouldReturnUnsettledDisputeWithNullFinancials() {
        when(disputeRepository.findById(9)).thenReturn(Optional.of(unsettledDispute));
        when(contractMilestoneRepository.findByContractIdOrderByOrderIndexAsc(43))
                .thenReturn(List.of());
        when(caseAttachmentRepository.findByOwnerTypeAndOwnerIdOrderByCreatedAtAsc("DISPUTE", 9L))
                .thenReturn(List.of());
        when(walletTransactionRepository.findByReferenceTypeAndReferenceIdOrderByCreatedAtAsc("DISPUTE", 9L))
                .thenReturn(List.of());

        AdminDisputeDetail result = service.getDisputeDetail(9);

        assertEquals(9, result.getDisputeId());
        assertEquals(DisputeEntity.STATUS_STAFF_REVIEWING, result.getStatus());
        assertNull(result.getExpertPayoutPercentage());
        assertNull(result.getExpertPayoutAmount());
        assertNull(result.getBusinessRefundAmount());
        assertNull(result.getSettlementExecutedAt());
        assertNull(result.getSettlementWalletTransactionId());
    }

    @Test
    void getDisputeDetail_shouldThrowNotFoundForMissingDispute() {
        when(disputeRepository.findById(999)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> service.getDisputeDetail(999));
        assertEquals("KHONG TIM THAY DISPUTE", ex.getMessage());
    }
}
