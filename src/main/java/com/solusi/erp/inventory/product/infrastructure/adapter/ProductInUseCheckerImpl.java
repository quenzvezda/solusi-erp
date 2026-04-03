package com.solusi.erp.inventory.product.infrastructure.adapter;

import com.solusi.erp.inventory.product.domain.port.ProductInUseChecker;
import com.solusi.erp.inventory.adjustment.infrastructure.persistence.StockAdjustmentLineJpaRepository;

public class ProductInUseCheckerImpl implements ProductInUseChecker {

    private final StockAdjustmentLineJpaRepository stockAdjustmentLineJpaRepository;

    public ProductInUseCheckerImpl(StockAdjustmentLineJpaRepository stockAdjustmentLineJpaRepository) {
        this.stockAdjustmentLineJpaRepository = stockAdjustmentLineJpaRepository;
    }

    @Override
    public boolean isUsed(Long productId) {
        return stockAdjustmentLineJpaRepository.existsByProductId(productId);
    }
}
