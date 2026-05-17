package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

@FunctionalInterface
public interface ConfirmVendorBillUseCase {
    void execute(Long id);
}
