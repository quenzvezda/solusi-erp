package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FindPurchaseReturnSerialsUseCase {

    Page<ReturnableSerialRow> execute(Long goodsReceiptId, Long goodsReceiptLineId, String keyword,
                                      List<String> excludedSelectionKeys, Pageable pageable);
}
