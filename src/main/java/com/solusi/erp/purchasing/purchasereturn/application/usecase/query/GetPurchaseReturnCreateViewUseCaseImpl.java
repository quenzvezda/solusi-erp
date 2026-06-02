package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;

import java.util.List;

public class GetPurchaseReturnCreateViewUseCaseImpl implements GetPurchaseReturnCreateViewUseCase {

    private final PurchaseReturnSourceQueryPort queryPort;

    public GetPurchaseReturnCreateViewUseCaseImpl(PurchaseReturnSourceQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public PurchaseReturnCreateView execute(Long goodsReceiptId) {
        EligibleGoodsReceiptRow source = queryPort.findEligibleGoodsReceiptById(goodsReceiptId)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.source.ineligible"));
        return new PurchaseReturnCreateView(
                source,
                queryPort.findReturnableGrLineSlices(goodsReceiptId, null, List.of())
        );
    }
}
