package com.solusi.erp.inventory.goodsissue.infrastructure.service;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueSourceResolver;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class GoodsIssueSourceResolverRegistry {

    private final Map<GoodsIssueReferenceType, GoodsIssueSourceResolver> resolvers;

    public GoodsIssueSourceResolverRegistry(List<GoodsIssueSourceResolver> resolvers) {
        this.resolvers = new EnumMap<>(GoodsIssueReferenceType.class);
        resolvers.forEach(resolver -> {
            if (this.resolvers.putIfAbsent(resolver.getReferenceType(), resolver) != null) {
                throw new DomainException("msg.error.common.duplicate");
            }
        });
    }

    public GoodsIssueSourceResolver getResolver(GoodsIssueReferenceType referenceType) {
        GoodsIssueSourceResolver resolver = resolvers.get(referenceType);
        if (resolver == null) {
            throw new DomainException("msg.error.gi.reference.unsupported");
        }
        return resolver;
    }
}
