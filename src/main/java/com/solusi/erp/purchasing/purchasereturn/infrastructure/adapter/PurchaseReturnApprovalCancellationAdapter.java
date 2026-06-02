package com.solusi.erp.purchasing.purchasereturn.infrastructure.adapter;

import com.solusi.erp.common.approval.application.usecase.CancelApprovalRequestUseCase;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnApprovalCancellationPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PurchaseReturnApprovalCancellationAdapter implements PurchaseReturnApprovalCancellationPort {

    private final CancelApprovalRequestUseCase cancelApprovalRequestUseCase;

    @Override
    public void cancel(Long purchaseReturnId, Long actorId, String notes) {
        cancelApprovalRequestUseCase.execute("PURCHASE_RETURN", purchaseReturnId, actorId, notes);
    }
}
