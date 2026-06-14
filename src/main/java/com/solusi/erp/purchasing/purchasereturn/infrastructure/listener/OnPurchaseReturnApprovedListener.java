package com.solusi.erp.purchasing.purchasereturn.infrastructure.listener;

import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturn;
import com.solusi.erp.purchasing.purchasereturn.domain.repository.PurchaseReturnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OnPurchaseReturnApprovedListener {

    private final PurchaseReturnRepository repository;

    @EventListener(condition = "#event.referenceType == 'PURCHASE_RETURN'")
    @Transactional
    public void handle(ApprovalCompletedEvent event) {
        PurchaseReturn purchaseReturn = repository.findById(event.getReferenceId())
                .orElseThrow(() -> new IllegalStateException(
                        "Purchase Return not found: " + event.getReferenceId()));
        purchaseReturn.approve();
        repository.save(purchaseReturn);
    }
}
