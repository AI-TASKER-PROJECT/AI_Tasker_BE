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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private AccessService accessService;
    @Mock private NotificationRepository notificationRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private NotificationService notificationService;

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
}
