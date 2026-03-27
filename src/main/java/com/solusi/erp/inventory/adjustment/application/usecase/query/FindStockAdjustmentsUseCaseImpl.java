package com.solusi.erp.inventory.adjustment.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;

public class FindStockAdjustmentsUseCaseImpl implements FindStockAdjustmentsUseCase {

    private final StockAdjustmentRepository repository;

    public FindStockAdjustmentsUseCaseImpl(StockAdjustmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<StockAdjustment> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
