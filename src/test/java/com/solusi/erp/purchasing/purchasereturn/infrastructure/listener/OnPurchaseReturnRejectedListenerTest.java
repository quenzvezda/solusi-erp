package com.solusi.erp.purchasing.purchasereturn.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalRejectedEvent;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OnPurchaseReturnRejectedListenerTest {

    @Test
    void handle_purchaseReturn_rejectsReleasesAndSaves() {
        PurchaseReturnRepository repository = mock(PurchaseReturnRepository.class);
        InventoryReservationService reservationService = mock(InventoryReservationService.class);
        PurchaseReturn purchaseReturn = mock(PurchaseReturn.class);
        when(repository.findById(1L)).thenReturn(Optional.of(purchaseReturn));

        new OnPurchaseReturnRejectedListener(repository, reservationService)
                .handle(new ApprovalRejectedEvent("PURCHASE_RETURN", 1L));

        verify(purchaseReturn).reject();
        verify(reservationService).release(ReservationOwnerType.PURCHASE_RETURN, 1L);
        verify(repository).save(purchaseReturn);
    }
}
