package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

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
class PurchaseReturnEventPublisherAdapterTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void publishApprovalRequestedShouldIncludePurchaseReturnViewPath() {
        PurchaseReturnEventPublisherAdapter adapter =
                new PurchaseReturnEventPublisherAdapter(applicationEventPublisher);

        adapter.publishApprovalRequested(21L, "PRT-001", 10L, 20L);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        ApprovalRequestedEvent event = (ApprovalRequestedEvent) captor.getValue();
        assertThat(event.getReferenceType()).isEqualTo("PURCHASE_RETURN");
        assertThat(event.getReferenceId()).isEqualTo(21L);
        assertThat(event.getReferenceCode()).isEqualTo("PRT-001");
        assertThat(event.getDocumentPath()).isEqualTo("/purchasing/purchase-returns/view/21");
    }
}
