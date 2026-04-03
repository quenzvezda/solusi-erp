package com.solusi.erp.inventory.product.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.product.domain.port.ProductLookupProvider;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;

public class ProductLookupProviderImpl implements ProductLookupProvider {

    private final JpaProductRepository jpaProductRepository;

    public ProductLookupProviderImpl(JpaProductRepository jpaProductRepository) {
        this.jpaProductRepository = jpaProductRepository;
    }

    @Override
    public LookupDto resolve(Long productId) {
        if (productId == null) return null;
        return jpaProductRepository.findById(productId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
