package com.solusi.erp.purchasing.purchaserequisition.domain.port;

public interface PurchaseRequisitionEventPublisher {
    void publishApprovalRequested(Long prId, String prCode, Long requesterId, Long approverId);
}
