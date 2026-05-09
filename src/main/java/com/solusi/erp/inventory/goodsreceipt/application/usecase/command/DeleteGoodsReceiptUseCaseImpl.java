package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptStatus;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;

public class DeleteGoodsReceiptUseCaseImpl implements DeleteGoodsReceiptUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public DeleteGoodsReceiptUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    @Override
    public void execute(Long id) {
        GoodsReceipt goodsReceipt = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gr.notfound"));
        if (goodsReceipt.getStatus() == GoodsReceiptStatus.COMPLETED) {
            throw new DomainException("msg.error.gr.completed.immutable");
        }
        goodsReceiptRepository.deleteById(id);
    }
}
