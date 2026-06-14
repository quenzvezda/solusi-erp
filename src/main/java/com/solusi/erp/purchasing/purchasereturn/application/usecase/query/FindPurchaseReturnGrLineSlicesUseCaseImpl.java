package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FindPurchaseReturnGrLineSlicesUseCaseImpl implements FindPurchaseReturnGrLineSlicesUseCase {

    private final PurchaseReturnSourceQueryPort queryPort;

    public FindPurchaseReturnGrLineSlicesUseCaseImpl(PurchaseReturnSourceQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public Page<ReturnableGrLineSlice> execute(Long goodsReceiptId, String keyword,
                                              List<String> excludedSelectionKeys, Pageable pageable) {
        return PurchaseReturnQueryPages.toPage(
                queryPort.findReturnableGrLineSlices(goodsReceiptId, keyword, excludedSelectionKeys),
                pageable
        );
    }
}
