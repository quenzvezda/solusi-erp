package com.solusi.erp.core.messaging.domain.model;

public record EventEnvelope<T>(
        String eventId,
        String eventType,
        int eventVersion,
        String source,
        String occurredAt,
        String correlationId,
        String aggregateType,
        String aggregateId,
        T payload) {
}
