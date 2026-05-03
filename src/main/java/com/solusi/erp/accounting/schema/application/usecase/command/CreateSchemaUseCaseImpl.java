package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.exception.DomainException;
import java.util.List;

public class CreateSchemaUseCaseImpl implements CreateSchemaUseCase {
    private final SchemaRepository repository;

    public CreateSchemaUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingSchema execute(SchemaEventType eventType, String description, Boolean isActive, List<AccountingSchemaLine> lines) {
        if (Boolean.TRUE.equals(isActive) && repository.existsByEventTypeAndIsActiveTrue(eventType)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        return repository.save(AccountingSchema.createNew(eventType, description, isActive, lines));
    }
}