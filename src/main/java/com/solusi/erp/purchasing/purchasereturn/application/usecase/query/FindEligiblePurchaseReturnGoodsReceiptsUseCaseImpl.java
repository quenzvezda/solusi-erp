package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public class FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl
        implements FindEligiblePurchaseReturnGoodsReceiptsUseCase {

    private final PurchaseReturnSourceQueryPort queryPort;

    public FindEligiblePurchaseReturnGoodsReceiptsUseCaseImpl(PurchaseReturnSourceQueryPort queryPort) {
        this.queryPort = queryPort;
    }

    @Override
    public Page<EligibleGoodsReceiptRow> execute(String keyword, Long supplierId, Long purchaseOrderId,
                                                LocalDate receiptDateFrom, LocalDate receiptDateTo,
                                                Pageable pageable) {
        return PurchaseReturnQueryPages.toPage(
                queryPort.findEligibleGoodsReceipts(
                        keyword, supplierId, purchaseOrderId, receiptDateFrom, receiptDateTo),
                pageable
        );
    }
}
