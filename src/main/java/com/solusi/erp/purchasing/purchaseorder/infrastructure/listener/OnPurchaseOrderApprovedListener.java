package com.solusi.erp.purchasing.purchaseorder.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OnPurchaseOrderApprovedListener {

    private final PurchaseOrderRepository purchaseOrderRepository;

    @EventListener(condition = "#event.referenceType == 'PURCHASE_ORDER'")
    public void handle(ApprovalCompletedEvent event) {
        log.info("Purchase Order approved: ID {}. Updating status...", event.getReferenceId());

        PurchaseOrder po = purchaseOrderRepository.findById(event.getReferenceId())
                .orElseThrow(() -> new IllegalStateException(
                        "Purchase Order not found: " + event.getReferenceId()));
        po.approve();
        purchaseOrderRepository.save(po);
    }
}
