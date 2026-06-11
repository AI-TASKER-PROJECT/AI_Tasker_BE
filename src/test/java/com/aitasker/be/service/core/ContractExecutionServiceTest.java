/*
 * NOTE FILE: src/test/java/com/aitasker/be/service/core/ContractExecutionServiceTest.java
 * Đây là file gì: File service chứa nghiệp vụ chính, điều phối repository và kiểm tra luật xử lý của hệ thống.
 * Mục đích note: giải thích các annotation và hàm chính để đọc hiểu chức năng code.
 */
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
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private ContractChangeRequestRepository changeRequestRepository;
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
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private StaffRepository staffRepository;
    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @Mock private SystemSettingRepository systemSettingRepository;

    // Note: Annotation này cung cấp metadata để Spring, JPA, Lombok, validation hoặc test xử lý tự động.
    @InjectMocks private ContractExecutionService contractExecutionService;

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `requestChange_shouldThrowWhenContractNotNegotiable` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
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
    // Note: Hàm `signNda_shouldThrowWhenContractNotActive` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
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

    // Note: Annotation này đánh dấu hàm test để JUnit thực thi.
    @Test
    // Note: Hàm `terminateContract_shouldThrowWhenReasonBlank` dùng để kiểm thử hành vi mong đợi, giúp phát hiện lỗi khi code thay đổi.
    void terminateContract_shouldThrowWhenReasonBlank() {
        ContractEntity contract = ContractEntity.builder().contractId(1).businessId(10).status("Active").build();
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
