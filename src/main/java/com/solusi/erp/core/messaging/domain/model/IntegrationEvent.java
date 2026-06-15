package com.solusi.erp.core.messaging.domain.model;

public interface IntegrationEvent {
    String topic();

    String messageKey();

    String eventType();

    int eventVersion();

    String source();

    String correlationId();

    String aggregateType();

    String aggregateId();

    Object payload();
}
