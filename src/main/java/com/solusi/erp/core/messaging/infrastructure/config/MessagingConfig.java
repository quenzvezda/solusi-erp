package com.solusi.erp.core.messaging.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solusi.erp.core.messaging.application.port.IntegrationEventPublisher;
import com.solusi.erp.core.messaging.domain.repository.OutboxEventRepository;
import com.solusi.erp.core.messaging.infrastructure.adapter.OutboxEventRepositoryAdapter;
import com.solusi.erp.core.messaging.infrastructure.persistence.OutboxEventJpaRepository;
import com.solusi.erp.core.messaging.infrastructure.persistence.OutboxEventPersistenceMapper;
import com.solusi.erp.core.messaging.infrastructure.publisher.OutboxIntegrationEventPublisher;
import com.solusi.erp.core.messaging.infrastructure.publisher.ScheduledOutboxKafkaPublisher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(MessagingProperties.class)
public class MessagingConfig {

    @Bean
    public OutboxEventPersistenceMapper outboxEventPersistenceMapper() {
        return new OutboxEventPersistenceMapper();
    }

    @Bean
    public OutboxEventRepository outboxEventRepository(
            OutboxEventJpaRepository jpaRepository,
            OutboxEventPersistenceMapper mapper) {
        return new OutboxEventRepositoryAdapter(jpaRepository, mapper);
    }

    @Bean
    public Clock messagingClock() {
        return Clock.systemUTC();
    }

    @Bean
    public IntegrationEventPublisher integrationEventPublisher(
            MessagingProperties properties,
            OutboxEventRepository outboxEventRepository,
            ObjectMapper objectMapper,
            Clock messagingClock) {
        if (!properties.enabled()) {
            return event -> {
            };
        }
        return new OutboxIntegrationEventPublisher(outboxEventRepository, objectMapper, messagingClock);
    }

    @Bean
    public ScheduledOutboxKafkaPublisher scheduledOutboxKafkaPublisher(
            OutboxEventRepository outboxEventRepository,
            KafkaTemplate<String, String> kafkaTemplate,
            MessagingProperties properties,
            Clock messagingClock) {
        return new ScheduledOutboxKafkaPublisher(
                outboxEventRepository,
                kafkaTemplate,
                properties,
                messagingClock);
    }
}
