package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import java.util.List;

public interface GetPurchaseReturnCreateViewUseCase {

    PurchaseReturnCreateView execute(Long goodsReceiptId);

    record PurchaseReturnCreateView(
            EligibleGoodsReceiptRow source,
            List<ReturnableGrLineSlice> lines
    ) {
    }
}
