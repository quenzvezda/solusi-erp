package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;

@FunctionalInterface
public interface FindGoodsReceiptsUseCase {
    Page<GoodsReceipt> execute(String keyword, Pageable pageable);
}
