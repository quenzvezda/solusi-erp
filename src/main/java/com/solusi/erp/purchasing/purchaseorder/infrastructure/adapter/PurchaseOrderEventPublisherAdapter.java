package com.solusi.erp.purchasing.purchaseorder.infrastructure.adapter;

import com.solusi.erp.core.event.ApprovalRequestedEvent;
import com.solusi.erp.purchasing.purchaseorder.domain.port.PurchaseOrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseOrderEventPublisherAdapter implements PurchaseOrderEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishApprovalRequested(Long poId, String poCode, Long requesterId, Long approverId) {
        eventPublisher.publishEvent(
                new ApprovalRequestedEvent("PURCHASE_ORDER", poId, poCode, requesterId, approverId));
    }
}
