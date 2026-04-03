package com.solusi.erp.inventory.productcategory.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.inventory.productcategory.domain.port.ProductCategoryLookupProvider;
import com.solusi.erp.inventory.productcategory.infrastructure.persistence.ProductCategoryJpaRepository;

public class ProductCategoryLookupProviderImpl implements ProductCategoryLookupProvider {

    private final ProductCategoryJpaRepository productCategoryJpaRepository;

    public ProductCategoryLookupProviderImpl(ProductCategoryJpaRepository productCategoryJpaRepository) {
        this.productCategoryJpaRepository = productCategoryJpaRepository;
    }

    @Override
    public LookupDto resolve(Long categoryId) {
        if (categoryId == null) return null;
        return productCategoryJpaRepository.findById(categoryId)
                .map(e -> new LookupDto(e.getId(), e.getName(), e.getCode()))
                .orElse(null);
    }
}
