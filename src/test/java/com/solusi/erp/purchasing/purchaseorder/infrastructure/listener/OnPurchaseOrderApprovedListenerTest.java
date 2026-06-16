package com.solusi.erp.purchasing.purchaseorder.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnPurchaseOrderApprovedListenerTest {

    @Test
    void handle_purchaseOrder_approvesAndSavesOnly() {
        PurchaseOrderRepository repository = mock(PurchaseOrderRepository.class);
        PurchaseOrder purchaseOrder = mock(PurchaseOrder.class);

        when(repository.findById(42L)).thenReturn(Optional.of(purchaseOrder));

        new OnPurchaseOrderApprovedListener(repository).handle(new ApprovalCompletedEvent("PURCHASE_ORDER", 42L, 99L));

        verify(purchaseOrder).approve();
        verify(repository).save(purchaseOrder);
    }
}
