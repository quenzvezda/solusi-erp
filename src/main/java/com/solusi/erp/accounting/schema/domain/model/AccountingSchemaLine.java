package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;

public record AccountingSchemaLine(Long id, JournalVariable variable, Long accountId, JournalPosition position) {
}