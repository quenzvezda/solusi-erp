package com.solusi.erp.accounting.coa.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteCoaUseCase {
    DeleteResult execute(Long id);
}
