package com.solusi.erp.core.messaging.infrastructure.config;

import com.solusi.erp.core.messaging.application.port.IntegrationEventPublisher;
import com.solusi.erp.core.messaging.infrastructure.persistence.OutboxEventJpaRepository;
import com.solusi.erp.core.messaging.infrastructure.publisher.OutboxIntegrationEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class MessagingConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(MessagingConfig.class, MocksConfig.class);

    @Test
    void disabledMessagingShouldProvideNoOpPublisher() {
        contextRunner
                .withPropertyValues("erp.messaging.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(IntegrationEventPublisher.class);
                    assertThat(context.getBean(IntegrationEventPublisher.class))
                            .isNotInstanceOf(OutboxIntegrationEventPublisher.class);
                });
    }

    @Test
    void enabledMessagingShouldProvideOutboxPublisher() {
        contextRunner
                .withPropertyValues("erp.messaging.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(IntegrationEventPublisher.class);
                    assertThat(context.getBean(IntegrationEventPublisher.class))
                            .isInstanceOf(OutboxIntegrationEventPublisher.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class MocksConfig {
        @Bean
        OutboxEventJpaRepository outboxEventJpaRepository() {
            return mock(OutboxEventJpaRepository.class);
        }
    }
}
