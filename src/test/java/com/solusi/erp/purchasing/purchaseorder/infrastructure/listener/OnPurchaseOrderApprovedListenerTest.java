package com.solusi.erp.purchasing.purchaseorder.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.core.messaging.application.port.IntegrationEventPublisher;
import com.solusi.erp.core.messaging.domain.model.IntegrationEvent;
import com.solusi.erp.purchasing.purchaseorder.application.service.PurchaseOrderApprovedEventFactory;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnPurchaseOrderApprovedListenerTest {

    @Test
    void handle_purchaseOrder_approvesSavesAndPublishesIntegrationEvent() {
        PurchaseOrderRepository repository = mock(PurchaseOrderRepository.class);
        PurchaseOrderApprovedEventFactory eventFactory = mock(PurchaseOrderApprovedEventFactory.class);
        IntegrationEventPublisher integrationEventPublisher = mock(IntegrationEventPublisher.class);
        PurchaseOrder purchaseOrder = mock(PurchaseOrder.class);
        IntegrationEvent integrationEvent = mock(IntegrationEvent.class);

        when(repository.findById(42L)).thenReturn(Optional.of(purchaseOrder));
        when(eventFactory.create(purchaseOrder, 99L)).thenReturn(integrationEvent);

        new OnPurchaseOrderApprovedListener(repository, eventFactory, integrationEventPublisher)
                .handle(new ApprovalCompletedEvent("PURCHASE_ORDER", 42L, 99L));

        verify(purchaseOrder).approve();
        verify(repository).save(purchaseOrder);
        verify(eventFactory).create(purchaseOrder, 99L);
        verify(integrationEventPublisher).publish(integrationEvent);
    }
}
