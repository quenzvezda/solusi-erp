package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;

@FunctionalInterface
public interface FindSchemasUseCase {
    Page<AccountingSchema> execute(String keyword, Pageable pageable);
}
