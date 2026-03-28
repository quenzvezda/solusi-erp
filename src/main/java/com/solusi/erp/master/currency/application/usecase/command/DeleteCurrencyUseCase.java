package com.solusi.erp.master.currency.application.usecase.command;

@FunctionalInterface
public interface DeleteCurrencyUseCase {
    void execute(Long id);
}
