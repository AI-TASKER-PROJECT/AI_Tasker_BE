package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.ForbiddenException;
import com.aitasker.be.dto.admin.AdminDisputeFilter;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class AdminDisputeDashboardAuthTest {

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

    @BeforeEach
    void setUp() {
        doThrow(new ForbiddenException("BAN KHONG CO QUYEN THUC HIEN CHUC NANG NAY"))
                .when(accessService).requireRole("ADMIN");
    }

    @Test
    void listDisputes_shouldRejectWhenNotAdmin() {
        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> service.listDisputes(new AdminDisputeFilter()));
        assertEquals("BAN KHONG CO QUYEN THUC HIEN CHUC NANG NAY", ex.getMessage());
    }

    @Test
    void getDisputeDetail_shouldRejectWhenNotAdmin() {
        ForbiddenException ex = assertThrows(ForbiddenException.class,
                () -> service.getDisputeDetail(8));
        assertEquals("BAN KHONG CO QUYEN THUC HIEN CHUC NANG NAY", ex.getMessage());
    }
}
