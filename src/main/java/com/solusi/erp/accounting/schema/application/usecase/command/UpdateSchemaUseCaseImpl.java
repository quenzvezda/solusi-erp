package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class UpdateSchemaUseCaseImpl implements UpdateSchemaUseCase {

    private final SchemaRepository repository;

    public UpdateSchemaUseCaseImpl(SchemaRepository repository) {
        this.repository = repository;
    }

    @Override
    public AccountingSchema execute(Long id, String description, Long debitAccountId,
                                    Long creditAccountId, Long taxAccountId, Boolean isActive) {
        AccountingSchema schema = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.schema.notfound"));
        if (Boolean.TRUE.equals(isActive)) {
            repository.findByEventTypeAndIsActiveTrue(schema.getEventType())
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new DomainException("msg.error.common.duplicate");
                    });
        }
        schema.update(description, debitAccountId, creditAccountId, taxAccountId, isActive);
        return repository.save(schema);
    }
}
