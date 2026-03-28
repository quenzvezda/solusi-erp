package com.solusi.erp.master.bankaccount.application.usecase.command;

@FunctionalInterface
public interface DeleteBankAccountUseCase {
    void execute(Long id);
}
