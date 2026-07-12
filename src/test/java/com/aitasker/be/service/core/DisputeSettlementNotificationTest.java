package com.aitasker.be.service.core;

import com.aitasker.be.event.DisputeSettlementCompletedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringJUnitConfig(DisputeSettlementNotificationTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DisputeSettlementNotificationTest {

    @Autowired private ApplicationEventPublisher eventPublisher;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private NotificationService notificationService;

    @Test
    void committedSettlement_shouldNotifyEveryAdminAfterCommit() {
        DisputeSettlementCompletedEvent event = event(List.of(10, 20));

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publishEvent(event);
            verify(notificationService, never()).notifyAdminDisputeSettlementReported(
                    10, 7, 1, 2, 3, 60, new BigDecimal("600.00"), new BigDecimal("400.00"), 99L);
        });

        verify(notificationService).notifyAdminDisputeSettlementReported(
                10, 7, 1, 2, 3, 60, new BigDecimal("600.00"), new BigDecimal("400.00"), 99L);
        verify(notificationService).notifyAdminDisputeSettlementReported(
                20, 7, 1, 2, 3, 60, new BigDecimal("600.00"), new BigDecimal("400.00"), 99L);
    }

    @Test
    void rolledBackSettlement_shouldNotNotifyAdmins() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            eventPublisher.publishEvent(event(List.of(10)));
            status.setRollbackOnly();
        });

        verify(notificationService, never()).notifyAdminDisputeSettlementReported(
                10, 7, 1, 2, 3, 60, new BigDecimal("600.00"), new BigDecimal("400.00"), 99L);
    }

    @Test
    void duplicateKeyRaceForOneAdmin_shouldNotBlockOtherAdmins() {
        doThrow(new DataIntegrityViolationException("duplicate idempotency key"))
                .when(notificationService).notifyAdminDisputeSettlementReported(
                        10, 7, 1, 2, 3, 60, new BigDecimal("600.00"), new BigDecimal("400.00"), 99L);

        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                eventPublisher.publishEvent(event(List.of(10, 20))));

        verify(notificationService, times(1)).notifyAdminDisputeSettlementReported(
                20, 7, 1, 2, 3, 60, new BigDecimal("600.00"), new BigDecimal("400.00"), 99L);
    }

    private DisputeSettlementCompletedEvent event(List<Integer> adminIds) {
        return new DisputeSettlementCompletedEvent(
                7, adminIds, 1, 2, 3, 60,
                new BigDecimal("600.00"), new BigDecimal("400.00"), 99L);
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {
        @Bean
        NotificationService notificationService() {
            return mock(NotificationService.class);
        }

        @Bean
        DisputeSettlementNotificationListener listener(NotificationService notificationService) {
            return new DisputeSettlementNotificationListener(notificationService);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {
                }

                @Override
                protected void doCommit(DefaultTransactionStatus status) {
                }

                @Override
                protected void doRollback(DefaultTransactionStatus status) {
                }
            };
        }
    }
}
