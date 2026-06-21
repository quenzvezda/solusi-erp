package com.solusi.erp.purchasing.purchaserequisition.infrastructure.adapter;

import com.solusi.erp.core.event.ApprovalRequestedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PurchaseRequisitionEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void publishApprovalRequestedShouldIncludePurchaseRequisitionViewPath() {
        PurchaseRequisitionEventPublisherAdapter adapter =
                new PurchaseRequisitionEventPublisherAdapter(applicationEventPublisher);

        adapter.publishApprovalRequested(11L, "PR-001", 10L, 20L);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        ApprovalRequestedEvent event = (ApprovalRequestedEvent) captor.getValue();
        assertThat(event.getReferenceType()).isEqualTo("PURCHASE_REQUISITION");
        assertThat(event.getReferenceId()).isEqualTo(11L);
        assertThat(event.getReferenceCode()).isEqualTo("PR-001");
        assertThat(event.getDocumentPath()).isEqualTo("/purchasing/purchase-requisitions/view/11");
    }
}
