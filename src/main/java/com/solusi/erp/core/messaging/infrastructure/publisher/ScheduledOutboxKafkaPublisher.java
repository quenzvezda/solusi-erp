package com.solusi.erp.core.messaging.infrastructure.publisher;

import com.solusi.erp.core.messaging.domain.model.OutboxEvent;
import com.solusi.erp.core.messaging.domain.repository.OutboxEventRepository;
import com.solusi.erp.core.messaging.infrastructure.config.MessagingProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@Slf4j
public class ScheduledOutboxKafkaPublisher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessagingProperties properties;
    private final Clock clock;

    public ScheduledOutboxKafkaPublisher(
            OutboxEventRepository repository,
            KafkaTemplate<String, String> kafkaTemplate,
            MessagingProperties properties,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "kafkaTemplate must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Scheduled(fixedDelayString = "${erp.messaging.outbox.fixed-delay-ms:5000}")
    public void publishPending() {
        if (!properties.enabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(clock);
        List<OutboxEvent> events = repository.findPublishableBatch(now, properties.outbox().batchSize());
        for (OutboxEvent event : events) {
            publishOne(event, now);
        }
    }

    private void publishOne(OutboxEvent event, LocalDateTime now) {
        if (event.getAttemptCount() >= properties.outbox().maxAttempts()) {
            log.warn("Outbox event {} skipped because max attempts {} reached",
                    event.getEventId(), properties.outbox().maxAttempts());
            return;
        }

        try {
            kafkaTemplate.send(event.getTopic(), event.getMessageKey(), event.getPayloadJson()).get();
            event.markPublished(now);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            markFailed(event, e, now);
        } catch (ExecutionException | RuntimeException e) {
            markFailed(event, e, now);
        }
        repository.save(event);
    }

    private void markFailed(OutboxEvent event, Exception exception, LocalDateTime now) {
        String error = exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage();
        int nextAttemptCount = event.getAttemptCount() + 1;
        LocalDateTime nextAttemptAt = nextAttemptCount >= properties.outbox().maxAttempts()
                ? now.plusYears(100)
                : now.plusSeconds(properties.outbox().retryDelaySeconds());
        event.markFailed(error, nextAttemptAt);
    }
}
