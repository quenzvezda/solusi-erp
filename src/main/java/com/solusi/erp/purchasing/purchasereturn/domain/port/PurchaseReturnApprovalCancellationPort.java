package com.solusi.erp.purchasing.purchasereturn.domain.port;

public interface PurchaseReturnApprovalCancellationPort {

    void cancel(Long purchaseReturnId, Long actorId, String notes);
}
