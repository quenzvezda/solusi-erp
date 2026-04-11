package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class CreateSchemaUseCaseImpl implements CreateSchemaUseCase {

    private final SchemaRepository repository;

    public CreateSchemaUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingSchema execute(SchemaEventType eventType, String description,
                                     Long debitAccountId, Long creditAccountId, Boolean isActive) {
        if (Boolean.TRUE.equals(isActive) && repository.existsByEventTypeAndIsActiveTrue(eventType)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        AccountingSchema schema = AccountingSchema.createNew(eventType, description,
                debitAccountId, creditAccountId, isActive);
        return repository.save(schema);
    }
}
