package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

public class CancelApprovedPurchaseReturnUseCaseImpl implements CancelApprovedPurchaseReturnUseCase {

    private final PurchaseReturnRepository repository;
    private final InventoryReservationService reservationService;

    public CancelApprovedPurchaseReturnUseCaseImpl(PurchaseReturnRepository repository,
                                                   InventoryReservationService reservationService) {
        this.repository = repository;
        this.reservationService = reservationService;
    }

    @Override
    public PurchaseReturn execute(Long id) {
        PurchaseReturn purchaseReturn = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));

        purchaseReturn.cancelApproved();
        reservationService.release(ReservationOwnerType.PURCHASE_RETURN, id);
        return repository.save(purchaseReturn);
    }
}
