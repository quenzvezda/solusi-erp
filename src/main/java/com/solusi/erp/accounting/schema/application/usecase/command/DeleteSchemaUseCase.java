package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;

@FunctionalInterface
public interface DeleteSchemaUseCase {
    DeleteResult execute(Long id);
}
