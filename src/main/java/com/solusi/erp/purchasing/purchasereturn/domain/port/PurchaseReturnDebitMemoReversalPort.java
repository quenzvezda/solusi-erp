package com.solusi.erp.purchasing.purchasereturn.domain.port;

public interface PurchaseReturnDebitMemoReversalPort {

    Long validateReversibleAndLock(Long purchaseReturnId);

    void cancelDebitMemo(Long debitMemoId);
}
