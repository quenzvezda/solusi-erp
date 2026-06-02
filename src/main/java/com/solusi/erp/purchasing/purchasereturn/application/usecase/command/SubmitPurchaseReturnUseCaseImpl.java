package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.stock.domain.model.ReservationOwnerType;
import com.solusi.erp.inventory.stock.domain.port.InventoryReservationService;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnEventPublisher;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

public class SubmitPurchaseReturnUseCaseImpl implements SubmitPurchaseReturnUseCase {

    private final PurchaseReturnRepository repository;
    private final InventoryReservationService reservationService;
    private final PurchaseReturnEventPublisher eventPublisher;

    public SubmitPurchaseReturnUseCaseImpl(PurchaseReturnRepository repository,
                                           InventoryReservationService reservationService,
                                           PurchaseReturnEventPublisher eventPublisher) {
        this.repository = repository;
        this.reservationService = reservationService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public PurchaseReturn execute(Long id, Long submitterUserId, Long requesterPartyId, Long approverId) {
        if (approverId == null) {
            throw new DomainException("msg.error.purchase-return.submit.approver-required");
        }

        PurchaseReturn purchaseReturn = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));

        purchaseReturn.validateSubmission(submitterUserId);
        reservationService.reserve(
                ReservationOwnerType.PURCHASE_RETURN,
                purchaseReturn.getId(),
                purchaseReturn.getCode(),
                PurchaseReturnReservationRequests.from(purchaseReturn)
        );
        purchaseReturn.submit(submitterUserId);
        PurchaseReturn saved = repository.save(purchaseReturn);
        eventPublisher.publishApprovalRequested(saved.getId(), saved.getCode(), requesterPartyId, approverId);
        return saved;
    }
}
