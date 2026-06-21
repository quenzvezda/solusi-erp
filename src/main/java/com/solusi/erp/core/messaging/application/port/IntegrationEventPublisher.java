package com.solusi.erp.core.messaging.application.port;

import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;

public interface IntegrationEventPublisher {
    void publish(IntegrationEvent event);
}
