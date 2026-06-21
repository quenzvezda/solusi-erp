package com.solusi.erp.core.messaging.infrastructure.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import com.solusi.erp.core.messaging.domain.model.OutboxEvent;
import com.solusi.erp.core.messaging.domain.model.OutboxStatus;
import com.solusi.erp.core.messaging.domain.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxIntegrationEventPublisherTest {

    private final CapturingOutboxEventRepository repository = new CapturingOutboxEventRepository();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T03:30:00Z"), ZoneOffset.UTC);
    private final OutboxIntegrationEventPublisher publisher =
            new OutboxIntegrationEventPublisher(repository, objectMapper, clock);

    @Test
    void publishShouldSavePendingOutboxEventWithJsonEnvelope() throws Exception {
        ApprovalActionOccurredSamplePayload payload = new ApprovalActionOccurredSamplePayload(
                55L,
                "PURCHASE_ORDER",
                42L,
                "PO-2606-00001",
                "APPROVE_AND_FINISH",
                "REQUESTER",
                "budi@example.com");
        IntegrationEvent event = new SampleIntegrationEvent(payload);

        publisher.publish(event);

        OutboxEvent saved = repository.savedEvent;
        assertThat(saved).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getTopic()).isEqualTo("erp.approval.events.v1");
        assertThat(saved.getMessageKey()).isEqualTo("55");
        assertThat(saved.getEventType()).isEqualTo("ApprovalActionOccurred");
        assertThat(saved.getEventVersion()).isEqualTo(1);
        assertThat(saved.getAggregateType()).isEqualTo("ApprovalRequest");
        assertThat(saved.getAggregateId()).isEqualTo("55");

        JsonNode envelope = objectMapper.readTree(saved.getPayloadJson());
        assertThat(envelope.get("eventId").asText()).isNotBlank();
        assertThat(envelope.get("eventType").asText()).isEqualTo("ApprovalActionOccurred");
        assertThat(envelope.get("eventVersion").asInt()).isEqualTo(1);
        assertThat(envelope.get("source").asText()).isEqualTo("erp-monolith");
        assertThat(envelope.get("occurredAt").asText()).isEqualTo("2026-06-15T03:30:00Z");
        assertThat(envelope.get("correlationId").asText()).isEqualTo("corr-55");
        assertThat(envelope.get("aggregateType").asText()).isEqualTo("ApprovalRequest");
        assertThat(envelope.get("aggregateId").asText()).isEqualTo("55");
        assertThat(envelope.path("payload").path("notificationTargetEmail").asText()).isEqualTo("budi@example.com");
    }

    private record ApprovalActionOccurredSamplePayload(
            Long approvalRequestId,
            String referenceType,
            Long referenceId,
            String referenceCode,
            String action,
            String notificationTargetRole,
            String notificationTargetEmail) {
    }

    private record SampleIntegrationEvent(ApprovalActionOccurredSamplePayload payload) implements IntegrationEvent {
        @Override
        public String topic() {
            return "erp.approval.events.v1";
        }

        @Override
        public String messageKey() {
            return "55";
        }

        @Override
        public String eventType() {
            return "ApprovalActionOccurred";
        }

        @Override
        public int eventVersion() {
            return 1;
        }

        @Override
        public String source() {
            return "erp-monolith";
        }

        @Override
        public String correlationId() {
            return "corr-55";
        }

        @Override
        public String aggregateType() {
            return "ApprovalRequest";
        }

        @Override
        public String aggregateId() {
            return "55";
        }
    }

    private static final class CapturingOutboxEventRepository implements OutboxEventRepository {
        private OutboxEvent savedEvent;

        @Override
        public OutboxEvent save(OutboxEvent event) {
            this.savedEvent = event;
            return event;
        }

        @Override
        public java.util.List<OutboxEvent> findPublishableBatch(java.time.LocalDateTime now, int batchSize) {
            return java.util.List.of();
        }

        @Override
        public Optional<OutboxEvent> findByEventId(String eventId) {
            return Optional.empty();
        }
    }
}
