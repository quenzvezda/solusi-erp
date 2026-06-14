package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.core.event.ApprovalRequestedEvent;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseReturnEventPublisherAdapter implements PurchaseReturnEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishApprovalRequested(Long purchaseReturnId, String purchaseReturnCode,
                                         Long requesterId, Long approverId) {
        eventPublisher.publishEvent(new ApprovalRequestedEvent(
                "PURCHASE_RETURN", purchaseReturnId, purchaseReturnCode, requesterId, approverId));
    }
}
