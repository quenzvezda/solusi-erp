package com.solusi.erp.master.tax.application.usecase.command;

@FunctionalInterface
public interface DeleteTaxUseCase {
    void execute(Long id);
}
