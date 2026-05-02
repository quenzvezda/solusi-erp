package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;

@FunctionalInterface
public interface UpdateSchemaUseCase {
    AccountingSchema execute(Long id, String description, Long debitAccountId,
                             Long creditAccountId, Long taxAccountId, Boolean isActive);

    default AccountingSchema execute(Long id, String description, Long debitAccountId,
                                     Long creditAccountId, Boolean isActive) {
        return execute(id, description, debitAccountId, creditAccountId, null, isActive);
    }
}
