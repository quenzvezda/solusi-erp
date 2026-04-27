package com.solusi.erp.inventory.goodsreceipt.infrastructure.service;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptSourceResolver;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GoodsReceiptSourceResolverRegistry {

    private final Map<GoodsReceiptReferenceType, GoodsReceiptSourceResolver> resolvers;

    public GoodsReceiptSourceResolverRegistry(List<GoodsReceiptSourceResolver> resolvers) {
        this.resolvers = new EnumMap<>(GoodsReceiptReferenceType.class);
        resolvers.forEach(resolver -> {
            if (this.resolvers.putIfAbsent(resolver.getReferenceType(), resolver) != null) {
                throw new DomainException("msg.error.common.duplicate");
            }
        });
    }

    public GoodsReceiptSourceResolver getResolver(GoodsReceiptReferenceType referenceType) {
        GoodsReceiptSourceResolver resolver = resolvers.get(referenceType);
        if (resolver == null) {
            throw new DomainException("msg.error.gr.reference.unsupported");
        }
        return resolver;
    }
}
