package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;

import java.util.Optional;

@FunctionalInterface
public interface GetSchemaEditViewUseCase {
    Optional<AccountingSchema> execute(Long id);
}
