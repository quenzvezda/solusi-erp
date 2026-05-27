package com.solusi.erp.purchasing.purchaserequisition.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalRejectedEvent;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OnPurchaseRequisitionRejectedListener {

    private final PurchaseRequisitionRepository purchaseRequisitionRepository;

    @EventListener(condition = "#event.referenceType == 'PURCHASE_REQUISITION'")
    public void handle(ApprovalRejectedEvent event) {
        log.info("Purchase Requisition rejected: ID {}. Updating status...", event.getReferenceId());

        PurchaseRequisition pr = purchaseRequisitionRepository.findById(event.getReferenceId())
                .orElseThrow(() -> new IllegalStateException(
                        "Purchase Requisition not found: " + event.getReferenceId()));
        pr.reject();
        purchaseRequisitionRepository.save(pr);
    }
}
