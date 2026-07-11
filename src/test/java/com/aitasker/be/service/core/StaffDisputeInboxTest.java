package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.dto.core.*;
import com.aitasker.be.entity.*;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StaffDisputeInboxTest {

    @Mock private AccessService accessService;
    @Mock private StaffRepository staffRepository;
    @Mock private DisputeRepository disputeRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private JobRepository jobRepository;
    @Mock private JobDomainRepository jobDomainRepository;
    @Mock private JobSkillRepository jobSkillRepository;
    @Mock private StaffDomainRepository staffDomainRepository;
    @Mock private StaffSkillRepository staffSkillRepository;
    @Mock private DomainRepository domainRepository;
    @Mock private SkillRepository skillRepository;

    @InjectMocks private StaffDisputeService staffDisputeService;

    private AccountEntity staffAccount;
    private StaffEntity staff;
    private DisputeEntity dispute1;
    private DisputeEntity dispute2;
    private ContractEntity contract;

    @BeforeEach
    void setUp() {
        staffAccount = AccountEntity.builder()
                .accountId(10).email("staff1@test.com").fullName("Staff One")
                .role(RoleEntity.builder().roleId(4).roleName("STAFF").build())
                .status("Approved").build();

        staff = StaffEntity.builder().staffId(1).accountId(10)
                .specialization("AI").build();

        contract = ContractEntity.builder().contractId(100).jobId(50)
                .businessId(1).expertId(2).build();

        dispute1 = DisputeEntity.builder()
                .disputeId(1).contractId(100).milestoneId(10)
                .status(DisputeEntity.STATUS_STAFF_REVIEWING)
                .initiatedBy("BUSINESS")
                .initiationType(DisputeEntity.INITIATION_BUSINESS_REJECTED_DELIVERABLE)
                .assignedStaffId(1)
                .createdAt(LocalDateTime.of(2026, 7, 10, 10, 0))
                .evidenceCollectionDueAt(LocalDateTime.now().plusHours(48))
                .staffSlaDueAt(LocalDateTime.now().plusHours(72))
                .build();

        dispute2 = DisputeEntity.builder()
                .disputeId(2).contractId(101).milestoneId(11)
                .status(DisputeEntity.STATUS_RESOLVED)
                .initiatedBy("EXPERT")
                .initiationType(DisputeEntity.INITIATION_EXPERT_SCOPE_CONCERN)
                .assignedStaffId(1)
                .staffDecisionPercentage(70)
                .staffDecidedAt(LocalDateTime.of(2026, 7, 9, 15, 0))
                .createdAt(LocalDateTime.of(2026, 7, 9, 8, 0))
                .build();

        lenient().when(accessService.currentAccount()).thenReturn(staffAccount);
        lenient().when(staffRepository.findByAccountId(10)).thenReturn(Optional.of(staff));
        lenient().when(contractRepository.findById(anyInt())).thenReturn(Optional.of(contract));
        lenient().when(jobDomainRepository.findByIdJobId(anyInt())).thenReturn(List.of());
        lenient().when(jobSkillRepository.findByIdJobId(anyInt())).thenReturn(List.of());
        lenient().when(domainRepository.findAllById(any())).thenReturn(List.of());
        lenient().when(skillRepository.findAllById(any())).thenReturn(List.of());
        lenient().when(staffDomainRepository.findByIdStaffId(anyInt())).thenReturn(List.of());
        lenient().when(staffSkillRepository.findByIdStaffId(anyInt())).thenReturn(List.of());
        lenient().when(jobRepository.findById(anyInt())).thenReturn(Optional.empty());
    }

    @Test
    void listDisputes_returnsAssignedDisputes() {
        List<DisputeEntity> disputes = List.of(dispute1, dispute2);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt", "disputeId"));
        Page<DisputeEntity> page = new PageImpl<>(disputes, pageable, disputes.size());
        when(disputeRepository.findByAssignedStaffIdOrderByCreatedAtDescDisputeIdDesc(
                eq(1), any(Pageable.class))).thenReturn(page);

        StaffDisputeListResponse result = staffDisputeService.listDisputes(
                StaffDisputeFilter.builder().page(0).size(20).build());

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void listDisputes_paginationCorrect() {
        List<DisputeEntity> disputes = List.of(dispute1);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt", "disputeId"));
        Page<DisputeEntity> page = new PageImpl<>(disputes, pageable, disputes.size());
        when(disputeRepository.findByAssignedStaffIdOrderByCreatedAtDescDisputeIdDesc(
                eq(1), any(Pageable.class))).thenReturn(page);

        StaffDisputeListResponse result = staffDisputeService.listDisputes(
                StaffDisputeFilter.builder().page(0).size(20).build());

        assertEquals(0, result.getPage());
        assertEquals(20, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void listDisputes_withStatusFilter() {
        List<DisputeEntity> disputes = List.of(dispute1);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt", "disputeId"));
        Page<DisputeEntity> page = new PageImpl<>(disputes, pageable, disputes.size());
        when(disputeRepository.findByAssignedStaffIdAndStatusOrderByCreatedAtDescDisputeIdDesc(
                eq(1), eq("STAFF_REVIEWING"), any(Pageable.class))).thenReturn(page);

        StaffDisputeListResponse result = staffDisputeService.listDisputes(
                StaffDisputeFilter.builder().page(0).size(20).status("STAFF_REVIEWING").build());

        assertEquals(1, result.getTotalElements());
        assertEquals(DisputeEntity.STATUS_STAFF_REVIEWING, result.getContent().get(0).getStatus());
    }

    @Test
    void listDisputes_negativePage_throwsException() {
        assertThrows(AppException.class, () -> staffDisputeService.listDisputes(
                StaffDisputeFilter.builder().page(-1).size(20).build()));
    }

    @Test
    void listDisputes_sizeOutOfBounds_throwsException() {
        assertThrows(AppException.class, () -> staffDisputeService.listDisputes(
                StaffDisputeFilter.builder().page(0).size(101).build()));
        assertThrows(AppException.class, () -> staffDisputeService.listDisputes(
                StaffDisputeFilter.builder().page(0).size(0).build()));
    }

    @Test
    void listDisputes_returnsFalseForUnsettledFields() {
        List<DisputeEntity> disputes = List.of(dispute1);
        PageRequest pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt", "disputeId"));
        Page<DisputeEntity> page = new PageImpl<>(disputes, pageable, disputes.size());
        when(disputeRepository.findByAssignedStaffIdOrderByCreatedAtDescDisputeIdDesc(
                eq(1), any(Pageable.class))).thenReturn(page);

        StaffDisputeListResponse result = staffDisputeService.listDisputes(
                StaffDisputeFilter.builder().page(0).size(20).build());

        StaffDisputeListItem item = result.getContent().get(0);
        assertFalse(item.isStaffDecisionMade());
        assertNull(item.getStaffDecidedAt());
    }
}
