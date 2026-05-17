package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import java.util.List;

@FunctionalInterface
public interface CreateSchemaUseCase {
    AccountingSchema execute(SchemaEventType eventType, String description, Boolean isActive, List<AccountingSchemaLine> lines);
}