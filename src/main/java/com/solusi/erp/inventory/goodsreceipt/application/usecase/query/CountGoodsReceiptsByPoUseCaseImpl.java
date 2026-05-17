package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;

public class CountGoodsReceiptsByPoUseCaseImpl implements CountGoodsReceiptsByPoUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public CountGoodsReceiptsByPoUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    @Override
    public long execute(Long poId) {
        return goodsReceiptRepository.countByPoId(poId);
    }
}
