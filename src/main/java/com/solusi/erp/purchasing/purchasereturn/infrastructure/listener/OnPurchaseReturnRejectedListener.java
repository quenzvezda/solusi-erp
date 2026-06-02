package com.solusi.erp.purchasing.purchasereturn.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalRejectedEvent;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OnPurchaseReturnRejectedListener {

    private final PurchaseReturnRepository repository;
    private final InventoryReservationService reservationService;

    @EventListener(condition = "#event.referenceType == 'PURCHASE_RETURN'")
    @Transactional
    public void handle(ApprovalRejectedEvent event) {
        PurchaseReturn purchaseReturn = repository.findById(event.getReferenceId())
                .orElseThrow(() -> new IllegalStateException(
                        "Purchase Return not found: " + event.getReferenceId()));
        purchaseReturn.reject();
        reservationService.release(ReservationOwnerType.PURCHASE_RETURN, event.getReferenceId());
        repository.save(purchaseReturn);
    }
}
