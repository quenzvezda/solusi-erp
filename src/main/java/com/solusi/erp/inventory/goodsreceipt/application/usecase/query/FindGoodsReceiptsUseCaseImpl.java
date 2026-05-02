package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;

public class FindGoodsReceiptsUseCaseImpl implements FindGoodsReceiptsUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;

    public FindGoodsReceiptsUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
    }

    @Override
    public Page<GoodsReceipt> execute(String keyword,
                                      GoodsReceiptReferenceType referenceType,
                                      Long referenceId,
                                      Pageable pageable) {
        return goodsReceiptRepository.findAll(keyword, referenceType, referenceId, pageable);
    }
}
