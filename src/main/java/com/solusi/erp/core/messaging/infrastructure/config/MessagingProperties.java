package com.solusi.erp.core.messaging.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp.messaging")
public record MessagingProperties(
        boolean enabled,
        Outbox outbox,
        Kafka kafka) {

    public MessagingProperties {
        if (outbox == null) {
            outbox = new Outbox(25, 5_000L, 60L, 10);
        }
        if (kafka == null) {
            kafka = new Kafka("erp.procurement.events.v1");
        }
    }

    public record Outbox(
            int batchSize,
            long fixedDelayMs,
            long retryDelaySeconds,
            int maxAttempts) {
    }

    public record Kafka(String defaultTopic) {
    }
}
