package com.solusi.erp.core.messaging.infrastructure.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solusi.erp.core.messaging.application.port.IntegrationEventPublisher;
import com.solusi.erp.core.messaging.domain.model.EventEnvelope;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import com.solusi.erp.core.messaging.domain.model.OutboxEvent;
import com.solusi.erp.core.messaging.domain.repository.OutboxEventRepository;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

public class OutboxIntegrationEventPublisher implements IntegrationEventPublisher {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OutboxIntegrationEventPublisher(
            OutboxEventRepository repository,
            ObjectMapper objectMapper,
            Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void publish(IntegrationEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        String eventId = UUID.randomUUID().toString();
        String correlationId = event.correlationId() == null || event.correlationId().isBlank()
                ? eventId
                : event.correlationId();
        String occurredAt = DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now(clock));

        EventEnvelope<Object> envelope = new EventEnvelope<>(
                eventId,
                event.eventType(),
                event.eventVersion(),
                event.source(),
                occurredAt,
                correlationId,
                event.aggregateType(),
                event.aggregateId(),
                event.payload());

        repository.save(OutboxEvent.pending(
                eventId,
                event.eventType(),
                event.eventVersion(),
                event.aggregateType(),
                event.aggregateId(),
                event.topic(),
                event.messageKey(),
                serialize(envelope)));
    }

    private String serialize(EventEnvelope<Object> envelope) {
        try {
            return objectMapper.writeValueAsString(envelope);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize integration event envelope", e);
        }
    }
}
