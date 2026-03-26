package com.solusi.erp.inventory.brand.application.usecase.command;

@FunctionalInterface
public interface DeleteBrandUseCase {
    void execute(Long id);
}
