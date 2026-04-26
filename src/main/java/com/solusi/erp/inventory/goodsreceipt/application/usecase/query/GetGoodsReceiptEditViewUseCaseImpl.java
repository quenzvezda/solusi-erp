package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;

import java.util.Optional;

public class GetGoodsReceiptEditViewUseCaseImpl implements GetGoodsReceiptEditViewUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public GetGoodsReceiptEditViewUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    @Override
    public Optional<GoodsReceipt> execute(Long id) {
        return goodsReceiptRepository.findById(id);
    }
}
