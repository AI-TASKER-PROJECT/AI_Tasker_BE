package com.aitasker.be.service.core;

import com.aitasker.be.entity.NotificationEntity;
import com.aitasker.be.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private AccessService accessService;
    @Mock private NotificationRepository notificationRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private NotificationService notificationService;

    @Test
    void notifyAdminDisputeSettlementReported_shouldPersistIdempotencyKeyAndPushOnce() {
        when(notificationRepository.findByIdempotencyKey("DISPUTE_SETTLEMENT_REPORTED:1:20"))
                .thenReturn(Optional.empty());
        when(notificationRepository.saveAndFlush(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity saved = invocation.getArgument(0);
            saved.setNotificationId(1);
            return saved;
        });

        notificationService.notifyAdminDisputeSettlementReported(
                20, 99, 1, 2, 3, 60,
                new BigDecimal("600.00"), new BigDecimal("400.00"), 77L);

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).saveAndFlush(captor.capture());
        assertEquals("DISPUTE_SETTLEMENT_REPORTED:1:20", captor.getValue().getIdempotencyKey());
        assertEquals("DISPUTE_SETTLEMENT_REPORTED", captor.getValue().getType());
        verify(messagingTemplate).convertAndSendToUser(
                org.mockito.ArgumentMatchers.eq("20"),
                org.mockito.ArgumentMatchers.eq("/queue/notifications"),
                any());
    }

    @Test
    void notifyAdminDisputeSettlementReported_replayedEventShouldNotPersistOrPushAgain() {
        NotificationEntity existing = NotificationEntity.builder()
                .notificationId(5)
                .receiverAccountId(20)
                .type("DISPUTE_SETTLEMENT_REPORTED")
                .title("Báo cáo quyết toán tranh chấp")
                .message("Đã quyết toán")
                .idempotencyKey("DISPUTE_SETTLEMENT_REPORTED:1:20")
                .isRead(false)
                .build();
        when(notificationRepository.findByIdempotencyKey("DISPUTE_SETTLEMENT_REPORTED:1:20"))
                .thenReturn(Optional.of(existing));

        notificationService.notifyAdminDisputeSettlementReported(
                20, 99, 1, 2, 3, 60,
                new BigDecimal("600.00"), new BigDecimal("400.00"), 77L);

        verify(notificationRepository, never()).saveAndFlush(any(NotificationEntity.class));
        verify(messagingTemplate, never()).convertAndSendToUser(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                any());
    }

    @Test
    void notifyDeliverableSubmitted_shouldBuildTargetUrlWithContractAndMilestone() {
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity saved = invocation.getArgument(0);
            saved.setNotificationId(1);
            return saved;
        });

        notificationService.notifyDeliverableSubmitted(20, 99, 1, 3, 11, "Milestone X");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertEquals("/contracts/1/workspace?milestoneId=3", saved.getTargetUrl());
        assertEquals("DELIVERABLE_SUBMITTED", saved.getType());
        assertEquals(20, saved.getReceiverAccountId());
        assertEquals(99, saved.getActorAccountId());
    }

    @Test
    void notifyDeliverableSubmitted_shouldSerializeMetadataWithContractMilestoneAndDeliverable() throws Exception {
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity saved = invocation.getArgument(0);
            saved.setNotificationId(1);
            return saved;
        });

        notificationService.notifyDeliverableSubmitted(20, 99, 1, 3, 11, "Milestone X");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertNotNull(saved.getMetadata());
        ObjectMapper mapper = new ObjectMapper();
        Map<?, ?> metadata = mapper.readValue(saved.getMetadata(), Map.class);
        assertEquals(1, metadata.get("contractId"));
        assertEquals(3, metadata.get("milestoneId"));
        assertEquals(11, metadata.get("deliverableId"));
    }

    @Test
    void notifyDeliverableSubmitted_shouldIncludeMilestoneNameInMessage() {
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity saved = invocation.getArgument(0);
            saved.setNotificationId(1);
            return saved;
        });

        notificationService.notifyDeliverableSubmitted(20, 99, 1, 3, 11, "Implement API");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertTrue(saved.getMessage().contains("Implement API"));
    }

    @Test
    void notifyWithdrawalRejected_shouldIncludeAdminReason() {
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity saved = invocation.getArgument(0);
            saved.setNotificationId(1);
            return saved;
        });

        notificationService.notifyWithdrawalRejected(20, 1, 90L, new BigDecimal("120000"), "Invalid bank info");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertEquals("WITHDRAWAL_REJECTED", saved.getType());
        assertEquals("Rút tiền thất bại", saved.getTitle());
        assertTrue(saved.getMessage().contains("Yêu cầu rút 120000 VND của bạn đã bị admin từ chối."));
        assertTrue(saved.getMessage().contains("Lý do: Invalid bank info"));
        assertTrue(saved.getMessage().contains("Invalid bank info"));
        assertEquals("/wallet/withdrawals", saved.getTargetUrl());
    }

    @Test
    void financeAndAccountNotifications_shouldUseVietnameseDiacritics() {
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity saved = invocation.getArgument(0);
            saved.setNotificationId(1);
            return saved;
        });

        notificationService.notifyWalletTopupSucceeded(20, 123L, new BigDecimal("50000"));
        notificationService.notifyWithdrawalApproved(20, 1, 90L, new BigDecimal("120000"));
        notificationService.notifyWithdrawalReviewRequested(1, 20, 90L, new BigDecimal("120000"));
        notificationService.notifyJobPostQuotaConsumed(20, 20, 77L, "AI Assistant", 2);
        notificationService.notifyNewAccountCreated(1, 22, "New Business", "new@mail.com", "BUSINESS");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository, org.mockito.Mockito.times(5)).save(captor.capture());

        assertEquals("Nạp tiền thành công", captor.getAllValues().get(0).getTitle());
        assertTrue(captor.getAllValues().get(0).getMessage().contains("Ví của bạn đã được nạp"));
        assertEquals("Rút tiền thành công", captor.getAllValues().get(1).getTitle());
        assertEquals("Có yêu cầu rút tiền cần duyệt", captor.getAllValues().get(2).getTitle());
        assertEquals("Đã trừ quota đăng bài", captor.getAllValues().get(3).getTitle());
        assertEquals("Có tài khoản mới", captor.getAllValues().get(4).getTitle());
    }

    @Test
    void notifyNewAccountCreated_shouldTargetAdminAccountsPage() {
        when(notificationRepository.save(any(NotificationEntity.class))).thenAnswer(invocation -> {
            NotificationEntity saved = invocation.getArgument(0);
            saved.setNotificationId(1);
            return saved;
        });

        notificationService.notifyNewAccountCreated(1, 22, "New Business", "new@mail.com", "BUSINESS");

        ArgumentCaptor<NotificationEntity> captor = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(captor.capture());
        NotificationEntity saved = captor.getValue();
        assertEquals("NEW_ACCOUNT_CREATED", saved.getType());
        assertEquals("/admin/accounts", saved.getTargetUrl());
        assertEquals(1, saved.getReceiverAccountId());
        assertEquals(22, saved.getActorAccountId());
    }
}
