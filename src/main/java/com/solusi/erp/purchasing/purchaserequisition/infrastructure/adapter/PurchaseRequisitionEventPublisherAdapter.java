package com.solusi.erp.purchasing.purchaserequisition.infrastructure.adapter;

import com.solusi.erp.core.event.ApprovalRequestedEvent;
import com.solusi.erp.purchasing.purchaserequisition.domain.port.PurchaseRequisitionEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseRequisitionEventPublisherAdapter implements PurchaseRequisitionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishApprovalRequested(Long prId, String prCode, Long requesterId, Long approverId) {
        eventPublisher.publishEvent(
                new ApprovalRequestedEvent(
                        "PURCHASE_REQUISITION",
                        prId,
                        prCode,
                        "/purchasing/purchase-requisitions/view/" + prId,
                        requesterId,
                        approverId));
    }
}
