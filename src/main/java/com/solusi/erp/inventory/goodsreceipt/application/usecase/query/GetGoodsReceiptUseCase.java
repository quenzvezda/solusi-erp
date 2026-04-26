package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;

import java.util.Optional;

@FunctionalInterface
public interface GetGoodsReceiptUseCase {
    Optional<GoodsReceipt> execute(Long id);
}
