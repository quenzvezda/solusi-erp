package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

import java.util.Optional;

public class GetSchemaEditViewUseCaseImpl implements GetSchemaEditViewUseCase {

    private final SchemaRepository repository;

    public GetSchemaEditViewUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AccountingSchema> execute(Long id) {
        return repository.findById(id);
    }
}
