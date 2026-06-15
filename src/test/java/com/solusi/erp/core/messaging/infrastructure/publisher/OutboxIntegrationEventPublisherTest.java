package com.solusi.erp.core.messaging.infrastructure.publisher;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import com.solusi.erp.core.messaging.domain.model.OutboxEvent;
import com.solusi.erp.core.messaging.domain.model.OutboxStatus;
import com.solusi.erp.core.messaging.domain.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
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
        PurchaseOrderApprovedSamplePayload payload = new PurchaseOrderApprovedSamplePayload(
                42L,
                "PO-2606-00001",
                "Budi",
                "budi@example.com",
                new BigDecimal("15000000.00"));
        IntegrationEvent event = new SampleIntegrationEvent(payload);

        publisher.publish(event);

        OutboxEvent saved = repository.savedEvent;
        assertThat(saved).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(OutboxStatus.PENDING);
        assertThat(saved.getTopic()).isEqualTo("erp.procurement.events.v1");
        assertThat(saved.getMessageKey()).isEqualTo("42");
        assertThat(saved.getEventType()).isEqualTo("PurchaseOrderApproved");
        assertThat(saved.getEventVersion()).isEqualTo(1);
        assertThat(saved.getAggregateType()).isEqualTo("PurchaseOrder");
        assertThat(saved.getAggregateId()).isEqualTo("42");

        JsonNode envelope = objectMapper.readTree(saved.getPayloadJson());
        assertThat(envelope.get("eventId").asText()).isNotBlank();
        assertThat(envelope.get("eventType").asText()).isEqualTo("PurchaseOrderApproved");
        assertThat(envelope.get("eventVersion").asInt()).isEqualTo(1);
        assertThat(envelope.get("source").asText()).isEqualTo("erp-monolith");
        assertThat(envelope.get("occurredAt").asText()).isEqualTo("2026-06-15T03:30:00Z");
        assertThat(envelope.get("correlationId").asText()).isEqualTo("corr-42");
        assertThat(envelope.get("aggregateType").asText()).isEqualTo("PurchaseOrder");
        assertThat(envelope.get("aggregateId").asText()).isEqualTo("42");
        assertThat(envelope.path("payload").path("requesterEmail").asText()).isEqualTo("budi@example.com");
    }

    private record PurchaseOrderApprovedSamplePayload(
            Long poId,
            String poNumber,
            String requesterName,
            String requesterEmail,
            BigDecimal totalAmount) {
    }

    private record SampleIntegrationEvent(PurchaseOrderApprovedSamplePayload payload) implements IntegrationEvent {
        @Override
        public String topic() {
            return "erp.procurement.events.v1";
        }

        @Override
        public String messageKey() {
            return "42";
        }

        @Override
        public String eventType() {
            return "PurchaseOrderApproved";
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
            return "corr-42";
        }

        @Override
        public String aggregateType() {
            return "PurchaseOrder";
        }

        @Override
        public String aggregateId() {
            return "42";
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
