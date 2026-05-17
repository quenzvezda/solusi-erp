package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.exception.DomainException;
import java.util.List;

public class UpdateSchemaUseCaseImpl implements UpdateSchemaUseCase {
    private final SchemaRepository repository;

    public UpdateSchemaUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingSchema execute(Long id, String description, Boolean isActive, List<AccountingSchemaLine> lines) {
        AccountingSchema schema = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.notfound"));
        
        if (Boolean.TRUE.equals(isActive) && !Boolean.TRUE.equals(schema.getIsActive()) && repository.existsByEventTypeAndIsActiveTrue(schema.getEventType())) {
            throw new DomainException("msg.error.common.duplicate");
        }

        schema.update(description, isActive, lines);
        return repository.save(schema);
    }
}