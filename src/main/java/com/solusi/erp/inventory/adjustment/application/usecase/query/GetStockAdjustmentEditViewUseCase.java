package com.solusi.erp.inventory.adjustment.application.usecase.query;

import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;

import java.util.Optional;

@FunctionalInterface
public interface GetStockAdjustmentEditViewUseCase {
    Optional<StockAdjustment> execute(Long id);
}
