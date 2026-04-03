package com.solusi.erp.inventory.adjustment.application.usecase.command;

@FunctionalInterface
public interface DeleteStockAdjustmentUseCase {
    void execute(Long id);
}
