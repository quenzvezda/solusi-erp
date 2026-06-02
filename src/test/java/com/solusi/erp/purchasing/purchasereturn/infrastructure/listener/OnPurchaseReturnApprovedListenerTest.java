package com.solusi.erp.purchasing.purchasereturn.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnPurchaseReturnApprovedListenerTest {

    @Test
    void handle_purchaseReturn_approvesAndSaves() {
        PurchaseReturnRepository repository = mock(PurchaseReturnRepository.class);
        PurchaseReturn purchaseReturn = mock(PurchaseReturn.class);
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));

        new OnPurchaseReturnApprovedListener(repository)
                .handle(new ApprovalCompletedEvent("PURCHASE_RETURN", 1L));

        verify(purchaseReturn).approve();
        verify(repository).save(purchaseReturn);
    }
}
