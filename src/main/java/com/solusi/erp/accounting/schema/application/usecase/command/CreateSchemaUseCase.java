package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;

@FunctionalInterface
public interface CreateSchemaUseCase {
    AccountingSchema execute(SchemaEventType eventType, String description,
                              Long debitAccountId, Long creditAccountId, Boolean isActive);
}
