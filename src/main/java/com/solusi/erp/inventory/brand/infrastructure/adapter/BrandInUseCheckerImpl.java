package com.solusi.erp.inventory.brand.infrastructure.adapter;

import com.solusi.erp.inventory.brand.domain.port.BrandInUseChecker;
import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;

public class BrandInUseCheckerImpl implements BrandInUseChecker {

    private final JpaProductRepository productJpaRepository;

    public BrandInUseCheckerImpl(JpaProductRepository productJpaRepository) {
        this.productJpaRepository = productJpaRepository;
    }

    @Override
    public boolean isUsedByAnyProduct(Long brandId) {
        return productJpaRepository.existsByBrandId(brandId);
    }
}
