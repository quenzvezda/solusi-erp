package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class FindSchemasUseCaseImpl implements FindSchemasUseCase {

    private final SchemaRepository repository;

    public FindSchemasUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<AccountingSchema> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
