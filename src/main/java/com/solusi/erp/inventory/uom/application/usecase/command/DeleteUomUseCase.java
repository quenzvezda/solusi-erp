package com.solusi.erp.inventory.uom.application.usecase.command;

@FunctionalInterface
public interface DeleteUomUseCase {
    void execute(Long id);
}
