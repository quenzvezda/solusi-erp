package com.solusi.erp.purchasing.purchasereturn.domain.port;

public interface PurchaseReturnEventPublisher {

    void publishApprovalRequested(Long purchaseReturnId, String purchaseReturnCode,
                                  Long requesterId, Long approverId);
}
