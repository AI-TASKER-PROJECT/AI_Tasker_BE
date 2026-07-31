package com.aitasker.be.service.core;

import com.aitasker.be.common.exception.AppException;
import com.aitasker.be.entity.MilestoneEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MilestoneReviewSlaSchedulerTest {
    @Test
    void duration_shouldParseAndAddMinutesHoursAndDays() {
        LocalDateTime start = LocalDateTime.of(2026, 7, 30, 10, 0);

        assertEquals(start.plusMinutes(15), MilestoneReviewSlaDuration.parse("15:MINUTE").addTo(start));
        assertEquals(start.plusHours(2), MilestoneReviewSlaDuration.parse("2:HOUR").addTo(start));
        assertEquals(start.plusDays(3), MilestoneReviewSlaDuration.parse("3:DAY").addTo(start));
    }

    @Test
    void duration_shouldRejectZeroAndUnsupportedUnits() {
        assertThrows(AppException.class, () -> MilestoneReviewSlaDuration.parse("0:MINUTE"));
        assertThrows(AppException.class, () -> MilestoneReviewSlaDuration.parse("1:WEEK"));
    }

    @Test
    void scheduler_shouldInvokeBackendOwnedProcessor() {
        ContractExecutionService service = mock(ContractExecutionService.class);
        when(service.processDueReviewSla()).thenReturn(List.of(new MilestoneEntity()));
        MilestoneReviewSlaScheduler scheduler = new MilestoneReviewSlaScheduler(service);

        scheduler.processDueMilestoneReviews();

        verify(service).processDueReviewSla();
    }
}
