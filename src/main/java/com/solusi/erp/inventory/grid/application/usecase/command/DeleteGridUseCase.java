package com.solusi.erp.inventory.grid.application.usecase.command;

@FunctionalInterface
public interface DeleteGridUseCase {
    void execute(Long id);
}
