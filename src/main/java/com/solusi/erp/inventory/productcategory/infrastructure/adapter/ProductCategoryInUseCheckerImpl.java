package com.solusi.erp.inventory.productcategory.infrastructure.adapter;

import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.productcategory.domain.port.ProductCategoryInUseChecker;

public class ProductCategoryInUseCheckerImpl implements ProductCategoryInUseChecker {

    private final JpaProductRepository productJpaRepository;

    public ProductCategoryInUseCheckerImpl(JpaProductRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public boolean isUsedByAnyProduct(Long categoryId) {
        return productJpaRepository.existsByCategoryId(categoryId);
    }
}
