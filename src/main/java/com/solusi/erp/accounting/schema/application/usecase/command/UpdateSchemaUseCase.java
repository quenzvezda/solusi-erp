package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import java.util.List;

@FunctionalInterface
public interface UpdateSchemaUseCase {
    AccountingSchema execute(Long id, String description, Boolean isActive, List<AccountingSchemaLine> lines);
}