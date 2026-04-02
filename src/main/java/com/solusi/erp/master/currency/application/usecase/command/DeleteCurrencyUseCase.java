package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteCurrencyUseCase {
    DeleteResult execute(Long id);
}
