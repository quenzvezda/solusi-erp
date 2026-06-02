package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface FindEligiblePurchaseReturnGoodsReceiptsUseCase {

    Page<EligibleGoodsReceiptRow> execute(String keyword, Long supplierId, Long purchaseOrderId,
                                          LocalDate receiptDateFrom, LocalDate receiptDateTo,
                                          Pageable pageable);
}
