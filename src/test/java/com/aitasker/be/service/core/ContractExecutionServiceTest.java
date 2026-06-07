package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.AccountEntity;
import com.aitasker.be.entity.BusinessProfileEntity;
import com.aitasker.be.entity.ContractChangeRequestEntity;
import com.aitasker.be.entity.ContractEntity;
import com.aitasker.be.entity.RoleEntity;
import com.aitasker.be.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractExecutionServiceTest {

    @Mock private AccessService accessService;
    @Mock private BusinessProfileRepository businessProfileRepository;
    @Mock private ExpertProfileRepository expertProfileRepository;
    @Mock private ProposalRepository proposalRepository;
    @Mock private JobRepository jobRepository;
    @Mock private ContractRepository contractRepository;
    @Mock private ContractChangeRequestRepository changeRequestRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private AcceptanceCriteriaRepository criteriaRepository;
    @Mock private DeliverableRepository deliverableRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private DisputeRepository disputeRepository;
    @Mock private StaffRepository staffRepository;
    @Mock private SystemSettingRepository systemSettingRepository;

    @InjectMocks private ContractExecutionService contractExecutionService;

    @Test
    void requestChange_shouldThrowWhenContractNotNegotiable() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).status("Active").build();
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(businessProfileRepository.findByAccountId(99)).thenReturn(Optional.of(BusinessProfileEntity.builder().businessId(10).build()));

        ContractChangeRequestEntity input = ContractChangeRequestEntity.builder()
                .contractId(1)
                .changeType("BUDGET")
                .changeSummary("TANG NGAN SACH")
                .build();

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.requestChange(input));
        assertEquals("CONTRACT KHONG O TRANG THAI CHO PHEP REQUEST CHANGE", ex.getMessage());
    }

    @Test
    void updateTransactionStatus_shouldThrowWhenInvalidStatus() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.updateTransactionStatus(1L, "INVALID"));
        assertEquals("STATUS TRANSACTION KHONG HOP LE", ex.getMessage());
    }

    @Test
    void resolveDispute_shouldThrowWhenActionEmpty() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.resolveDispute(1, " "));
        assertEquals("PROPOSED ACTION KHONG DUOC DE TRONG", ex.getMessage());
    }

    @Test
    void signNda_shouldThrowWhenContractNotActive() {
        ContractEntity contract = ContractEntity.builder().contractId(1).expertId(5).status("Draft").build();
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));
        when(accessService.currentAccount()).thenReturn(
                AccountEntity.builder().accountId(99).role(RoleEntity.builder().roleName("EXPERT").build()).build()
        );
        when(expertProfileRepository.findByAccountId(99))
                .thenReturn(Optional.of(com.aitasker.be.entity.ExpertProfileEntity.builder().expertId(5).build()));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.signNda(1));
        assertEquals("CHI DUOC KY NDA KHI CONTRACT DA ACTIVE", ex.getMessage());
    }

    @Test
    void terminateContract_shouldThrowWhenReasonBlank() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).status("Active").build();
        when(contractRepository.findById(1)).thenReturn(Optional.of(contract));

        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.terminateContract(1, " "));
        assertEquals("LY DO CHAM DUT KHONG DUOC DE TRONG", ex.getMessage());
    }

    @Test
    void recordDemoTesting_shouldThrowWhenResultBlank() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.recordDemoTesting(1, " "));
        assertEquals("KET QUA DEMO TEST KHONG DUOC DE TRONG", ex.getMessage());
    }

    @Test
    void issueTechnicalReport_shouldThrowWhenReportBlank() {
        AppException ex = assertThrows(AppException.class, () -> contractExecutionService.issueTechnicalReport(1, " ", null));
        assertEquals("TECHNICAL REPORT KHONG DUOC DE TRONG", ex.getMessage());
    }

    @Test
    void processPaymentWebhook_shouldThrowWhenStatusInvalid() {
        AppException ex = assertThrows(AppException.class, () ->
                contractExecutionService.processPaymentWebhook(1L, "PENDING", null, null));
        assertEquals("PAYMENT STATUS KHONG HOP LE", ex.getMessage());
    }
}
