package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.service.GoodsReceiptSourceResolverRegistry;

public class GetGoodsReceiptCreateViewUseCaseImpl implements GetGoodsReceiptCreateViewUseCase {

    private final GoodsReceiptSourceResolverRegistry resolverRegistry;

    public GetGoodsReceiptCreateViewUseCaseImpl(GoodsReceiptSourceResolverRegistry resolverRegistry) {
        this.resolverRegistry = resolverRegistry;
    }

    @Override
    public GoodsReceipt execute(GoodsReceiptReferenceType referenceType, Long referenceId) {
        return resolverRegistry.getResolver(referenceType).resolve(referenceId);
    }
}
