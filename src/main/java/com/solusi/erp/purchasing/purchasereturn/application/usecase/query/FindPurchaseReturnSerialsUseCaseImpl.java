package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class FindPurchaseReturnSerialsUseCaseImpl implements FindPurchaseReturnSerialsUseCase {

    private final PurchaseReturnSourceQueryPort queryPort;

    public FindPurchaseReturnSerialsUseCaseImpl(PurchaseReturnSourceQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public Page<ReturnableSerialRow> execute(Long goodsReceiptId, Long goodsReceiptLineId, String keyword,
                                            List<String> excludedSelectionKeys, Pageable pageable) {
        return PurchaseReturnQueryPages.toPage(
                queryPort.findReturnableSerials(
                        goodsReceiptId, goodsReceiptLineId, keyword, excludedSelectionKeys),
                pageable
        );
    }
}
