package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;

import java.util.Optional;

public class GetGoodsReceiptUseCaseImpl implements GetGoodsReceiptUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public GetGoodsReceiptUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    @Override
    public Optional<GoodsReceipt> execute(Long id) {
        return goodsReceiptRepository.findById(id);
    }
}
