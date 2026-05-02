package com.solusi.erp.accounting.journal.application.policy;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;

import java.util.List;

public interface JournalPolicy {
    SchemaEventType supports();
    List<JournalLine> buildLines(AccountingSchema schema, JournalPostingCommand command);
}
