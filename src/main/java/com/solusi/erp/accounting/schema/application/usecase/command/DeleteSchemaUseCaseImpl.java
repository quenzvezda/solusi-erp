package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.port.SchemaInUseChecker;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;

public class DeleteSchemaUseCaseImpl implements DeleteSchemaUseCase {

    private final SchemaRepository repository;
    private final SchemaInUseChecker inUseChecker;

    public DeleteSchemaUseCaseImpl(SchemaRepository repository, SchemaInUseChecker inUseChecker) {
        this.repository = repository;
        this.inUseChecker = inUseChecker;
    }

    @Override
    public DeleteResult execute(Long id) {
        AccountingSchema schema = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.schema.notfound"));
        if (inUseChecker.isInUse(id)) {
            schema.softDelete();
            repository.save(schema);
            return DeleteResult.SOFT_DELETED;
        }
        repository.delete(id);
        return DeleteResult.HARD_DELETED;
    }
}
