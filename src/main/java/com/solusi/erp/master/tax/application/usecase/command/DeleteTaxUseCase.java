package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteTaxUseCase {
    DeleteResult execute(Long id);
}
