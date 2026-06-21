package com.solusi.erp.purchasing.purchaseorder.domain.port;

public interface PurchaseOrderEventPublisher {
    void publishApprovalRequested(Long poId, String poCode, String documentPath, Long requesterId, Long approverId);
}
