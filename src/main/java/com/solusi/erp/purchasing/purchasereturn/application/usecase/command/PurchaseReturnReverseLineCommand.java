package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

public record PurchaseReturnReverseLineCommand(
        Long originalMovementId,
        Long targetContainerId
) {
}
