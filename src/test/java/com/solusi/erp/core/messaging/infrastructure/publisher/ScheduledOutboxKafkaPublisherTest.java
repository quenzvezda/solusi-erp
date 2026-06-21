package com.solusi.erp.core.messaging.infrastructure.publisher;

import com.solusi.erp.core.messaging.domain.model.OutboxEvent;
import com.solusi.erp.core.messaging.domain.model.OutboxStatus;
import com.solusi.erp.core.messaging.domain.repository.OutboxEventRepository;
import com.solusi.erp.core.messaging.infrastructure.config.MessagingProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledOutboxKafkaPublisherTest {

    private OutboxEventRepository repository;
    private KafkaTemplate<String, String> kafkaTemplate;
    private Clock clock;

    @BeforeEach
    void setUp() {
        repository = mock(OutboxEventRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        clock = Clock.fixed(Instant.parse("2026-06-15T03:30:00Z"), ZoneOffset.UTC);
    }

    @Test
    void disabledPropertiesShouldNotCallRepositoryOrKafka() {
        ScheduledOutboxKafkaPublisher publisher = new ScheduledOutboxKafkaPublisher(
                repository,
                kafkaTemplate,
                properties(false, 25, 60, 10),
                clock);

        publisher.publishPending();

        verify(repository, never()).findPublishableBatch(any(), any(Integer.class));
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void successfulSendShouldMarkEventPublishedAndSave() {
        OutboxEvent event = pendingEvent();
        when(repository.findPublishableBatch(any(), eq(25))).thenReturn(List.of(event));
        when(kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayloadJson()))
                .thenReturn(CompletableFuture.completedFuture(null));
        ScheduledOutboxKafkaPublisher publisher = new ScheduledOutboxKafkaPublisher(
                repository,
                kafkaTemplate,
                properties(true, 25, 60, 10),
                clock);

        publisher.publishPending();

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.PUBLISHED);
        assertThat(event.getPublishedAt()).isEqualTo(LocalDateTime.of(2026, 6, 15, 3, 30));
        verify(repository).save(event);
    }

    @Test
    void failedSendShouldMarkEventFailedAndScheduleRetry() {
        OutboxEvent event = pendingEvent();
        when(repository.findPublishableBatch(any(), eq(25))).thenReturn(List.of(event));
        when(kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayloadJson()))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("broker down")));
        ScheduledOutboxKafkaPublisher publisher = new ScheduledOutboxKafkaPublisher(
                repository,
                kafkaTemplate,
                properties(true, 25, 60, 10),
                clock);

        publisher.publishPending();

        assertThat(event.getStatus()).isEqualTo(OutboxStatus.FAILED);
        assertThat(event.getAttemptCount()).isOne();
        assertThat(event.getLastError()).contains("broker down");
        assertThat(event.getNextAttemptAt()).isEqualTo(LocalDateTime.of(2026, 6, 15, 3, 31));
        verify(repository).save(event);
    }

    @Test
    void batchSizeShouldComeFromProperties() {
        when(repository.findPublishableBatch(any(), eq(7))).thenReturn(List.of());
        ScheduledOutboxKafkaPublisher publisher = new ScheduledOutboxKafkaPublisher(
                repository,
                kafkaTemplate,
                properties(true, 7, 60, 10),
                clock);

        publisher.publishPending();

        verify(repository).findPublishableBatch(any(LocalDateTime.class), eq(7));
    }

    private static MessagingProperties properties(boolean enabled, int batchSize, long retryDelaySeconds, int maxAttempts) {
        return new MessagingProperties(
                enabled,
                new MessagingProperties.Outbox(batchSize, 5_000L, retryDelaySeconds, maxAttempts),
                new MessagingProperties.Kafka("erp.approval.events.v1"));
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
