package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;

import java.util.Optional;

@FunctionalInterface
public interface GetGoodsReceiptEditViewUseCase {
    Optional<GoodsReceipt> execute(Long id);
}
