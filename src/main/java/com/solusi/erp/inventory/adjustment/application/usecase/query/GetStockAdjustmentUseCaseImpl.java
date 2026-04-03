package com.solusi.erp.inventory.adjustment.application.usecase.query;

import com.solusi.erp.inventory.adjustment.domain.model.StockAdjustment;
import com.solusi.erp.inventory.adjustment.domain.repository.StockAdjustmentRepository;

import java.util.Optional;

public class GetStockAdjustmentUseCaseImpl implements GetStockAdjustmentUseCase {

    private final StockAdjustmentRepository repository;

    public GetStockAdjustmentUseCaseImpl(StockAdjustmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StockAdjustment> execute(Long id) {
        return repository.findById(id);
    }
}
