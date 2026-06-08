package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnInventoryReversalPort;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnReversibleMovement;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;

import java.util.List;

public class GetPurchaseReturnReverseViewUseCaseImpl implements GetPurchaseReturnReverseViewUseCase {

    private final PurchaseReturnRepository purchaseReturnRepository;
    private final PurchaseReturnInventoryReversalPort inventoryReversalPort;

    public GetPurchaseReturnReverseViewUseCaseImpl(PurchaseReturnRepository purchaseReturnRepository,
                                                   PurchaseReturnInventoryReversalPort inventoryReversalPort) {
        this.purchaseReturnRepository = purchaseReturnRepository;
        this.inventoryReversalPort = inventoryReversalPort;
    }

    @Override
    public PurchaseReturnReverseView execute(Long id) {
        PurchaseReturn purchaseReturn = purchaseReturnRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));
        purchaseReturn.validateReversalHeader(
                purchaseReturn.getReturnDate(),
                "view",
                0L
        );
        return new PurchaseReturnReverseView(
                purchaseReturn,
                inventoryReversalPort.findReversibleMovements(purchaseReturn).stream()
                        .map(this::toLineView)
                        .toList()
        );
    }

    private PurchaseReturnReverseLineView toLineView(PurchaseReturnReversibleMovement movement) {
        return new PurchaseReturnReverseLineView(
                movement.originalMovementId(),
                movement.purchaseReturnLineId(),
                movement.productId(),
                movement.productName(),
                movement.productCode(),
                movement.quantity(),
                movement.sourceContainerId(),
                movement.sourceContainerName(),
                movement.sourceContainerCode(),
                movement.serialNumber()
        );
    }
}
