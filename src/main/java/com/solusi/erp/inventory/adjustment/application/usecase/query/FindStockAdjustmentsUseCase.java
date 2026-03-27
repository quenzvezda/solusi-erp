package com.solusi.erp.inventory.adjustment.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;

@FunctionalInterface
public interface FindStockAdjustmentsUseCase {
    Page<StockAdjustment> execute(String keyword, Pageable pageable);
}
