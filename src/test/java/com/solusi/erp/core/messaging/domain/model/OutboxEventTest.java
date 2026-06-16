package com.solusi.erp.core.messaging.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxEventTest {

    @Test
    void pendingShouldCreateAuditableEventReadyForPublishing() {
        OutboxEvent event = OutboxEvent.pending(
                "11111111-1111-1111-1111-111111111111",
                "ApprovalActionOccurred",
                1,
                "ApprovalRequest",
                "55",
                "erp.approval.events.v1",
                "55",
                "{\"eventType\":\"ApprovalActionOccurred\"}");

        assertThat(event.getEventId()).isEqualTo("11111111-1111-1111-1111-111111111111");
        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(event.getAttemptCount()).isZero();
        assertThat(event.getPublishedAt()).isNull();
        assertThat(event.getLastError()).isNull();
        assertThat(event.getTopic()).isEqualTo("erp.approval.events.v1");
        assertThat(event.getMessageKey()).isEqualTo("55");
    }

    @Test
    void markPublishedShouldRecordPublishedStatusAndTimestamp() {
        OutboxEvent event = pendingEvent();
        LocalDateTime publishedAt = LocalDateTime.of(2026, 6, 15, 10, 30);

        event.markPublished(publishedAt);

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(event.getLastError()).isNull();
    }

    @Test
    void markFailedShouldIncrementAttemptAndStoreTruncatedRetryMetadata() {
        OutboxEvent event = pendingEvent();
        LocalDateTime retryAt = LocalDateTime.of(2026, 6, 15, 10, 31);
        String longError = "x".repeat(1_500);

        event.markFailed(longError, retryAt);

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getAttemptCount()).isOne();
        assertThat(event.getLastError()).hasSize(1_000);
        assertThat(event.getNextAttemptAt()).isEqualTo(retryAt);
    }

    private static OutboxEvent pendingEvent() {
        return OutboxEvent.pending(
                "11111111-1111-1111-1111-111111111111",
                "ApprovalActionOccurred",
                1,
                "ApprovalRequest",
                "55",
                "erp.approval.events.v1",
                "55",
                "{\"eventType\":\"ApprovalActionOccurred\"}");
    }
}
