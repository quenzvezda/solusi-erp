package com.solusi.erp.inventory.product.infrastructure.adapter;

import com.solusi.erp.inventory.product.infrastructure.persistence.JpaProductRepository;
import com.solusi.erp.inventory.uom.domain.port.UomUsageChecker;

/**
 * Checks whether any Product references the given UoM as its base unit.
 */
public class ProductUomUsageChecker implements UomUsageChecker {

    private final JpaProductRepository productRepository;

    public ProductUomUsageChecker(JpaProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public boolean isUsed(Long uomId) {
        return productRepository.existsByUomId(uomId);
    }
}
