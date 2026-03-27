package com.solusi.erp.inventory.adjustment.application.usecase.command;

@FunctionalInterface
public interface ProcessStockAdjustmentUseCase {
    void execute(Long id);
}
