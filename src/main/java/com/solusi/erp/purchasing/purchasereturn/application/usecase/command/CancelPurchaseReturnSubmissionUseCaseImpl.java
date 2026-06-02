package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnApprovalCancellationPort;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

public class CancelPurchaseReturnSubmissionUseCaseImpl implements CancelPurchaseReturnSubmissionUseCase {

    private final PurchaseReturnRepository repository;
    private final InventoryReservationService reservationService;
    private final PurchaseReturnApprovalCancellationPort approvalCancellationPort;

    public CancelPurchaseReturnSubmissionUseCaseImpl(PurchaseReturnRepository repository,
                                                     InventoryReservationService reservationService,
                                                     PurchaseReturnApprovalCancellationPort approvalCancellationPort) {
        this.repository = repository;
        this.reservationService = reservationService;
        this.approvalCancellationPort = approvalCancellationPort;
    }

    @Override
    public PurchaseReturn execute(Long id, Long actorUserId, String notes) {
        PurchaseReturn purchaseReturn = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));

        purchaseReturn.cancelSubmission(actorUserId);
        approvalCancellationPort.cancel(id, actorUserId, notes);
        reservationService.release(ReservationOwnerType.PURCHASE_RETURN, id);
        return repository.save(purchaseReturn);
    }
}
