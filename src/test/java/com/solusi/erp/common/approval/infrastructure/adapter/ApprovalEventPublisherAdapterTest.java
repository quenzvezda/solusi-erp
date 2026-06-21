package com.solusi.erp.common.approval.infrastructure.adapter;

import com.solusi.erp.common.approval.application.service.ApprovalActionOccurredEventFactory;
import com.solusi.erp.common.approval.domain.model.ApprovalAction;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.core.event.ApprovalRejectedEvent;
import com.solusi.erp.core.messaging.application.port.IntegrationEventPublisher;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApprovalEventPublisherAdapterTest {

    private ApplicationEventPublisher springEventPublisher;
    private IntegrationEventPublisher integrationEventPublisher;
    private ApprovalActionOccurredEventFactory eventFactory;
    private IntegrationEvent integrationEvent;
    private ApprovalEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        springEventPublisher = mock(ApplicationEventPublisher.class);
        integrationEventPublisher = mock(IntegrationEventPublisher.class);
        eventFactory = mock(ApprovalActionOccurredEventFactory.class);
        integrationEvent = mock(IntegrationEvent.class);
        adapter = new ApprovalEventPublisherAdapter(springEventPublisher, integrationEventPublisher, eventFactory);
    }

    @Test
    void publishRequestedShouldEmitOnlyGenericIntegrationEvent() {
        ApprovalRequest request = request();
        when(eventFactory.create(request, ApprovalAction.REQUESTED)).thenReturn(integrationEvent);

        adapter.publishRequested(request);

        verify(springEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
        verify(integrationEventPublisher).publish(integrationEvent);
    }

    @Test
    void publishCompletedShouldEmitSpringCompletedAndGenericIntegrationEvent() {
        ApprovalRequest request = request();
        when(eventFactory.create(request, ApprovalAction.APPROVE_AND_FINISH)).thenReturn(integrationEvent);

        adapter.publishCompleted(request, 2L);

        ArgumentCaptor<ApprovalCompletedEvent> eventCaptor = ArgumentCaptor.forClass(ApprovalCompletedEvent.class);
        verify(springEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getReferenceType()).isEqualTo("NEWS");
        assertThat(eventCaptor.getValue().getReferenceId()).isEqualTo(100L);
        assertThat(eventCaptor.getValue().getActorId()).isEqualTo(2L);
        verify(integrationEventPublisher).publish(integrationEvent);
    }

    @Test
    void publishRejectedShouldEmitSpringRejectedAndGenericIntegrationEvent() {
        ApprovalRequest request = request();
        when(eventFactory.create(request, ApprovalAction.REJECTED)).thenReturn(integrationEvent);

        adapter.publishRejected(request, 2L);

        ArgumentCaptor<ApprovalRejectedEvent> eventCaptor = ArgumentCaptor.forClass(ApprovalRejectedEvent.class);
        verify(springEventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getReferenceType()).isEqualTo("NEWS");
        assertThat(eventCaptor.getValue().getReferenceId()).isEqualTo(100L);
        verify(integrationEventPublisher).publish(integrationEvent);
    }

    @Test
    void publishForwardedShouldEmitOnlyGenericIntegrationEvent() {
        ApprovalRequest request = request();
        when(eventFactory.create(request, ApprovalAction.FORWARD)).thenReturn(integrationEvent);

        adapter.publishForwarded(request, 2L, 3L);

        verify(springEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
        verify(integrationEventPublisher).publish(integrationEvent);
    }

    @Test
    void publishApprovedAndForwardedShouldEmitOnlyGenericIntegrationEvent() {
        ApprovalRequest request = request();
        when(eventFactory.create(request, ApprovalAction.APPROVE_AND_FORWARD)).thenReturn(integrationEvent);

        adapter.publishApprovedAndForwarded(request, 2L, 3L);

        verify(springEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
        verify(integrationEventPublisher).publish(integrationEvent);
    }

    private static ApprovalRequest request() {
        return ApprovalRequest.createNew("NEWS", 100L, "NEWS-100", "/common/news/view/100", 1L, 2L);
    }
}
