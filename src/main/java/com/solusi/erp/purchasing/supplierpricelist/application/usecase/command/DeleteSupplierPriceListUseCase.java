package com.solusi.erp.purchasing.supplierpricelist.application.usecase.command;

@FunctionalInterface
public interface DeleteSupplierPriceListUseCase {
    void execute(Long id);
}
