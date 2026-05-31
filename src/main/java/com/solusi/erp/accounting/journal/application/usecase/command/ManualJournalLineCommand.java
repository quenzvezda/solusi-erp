package com.solusi.erp.accounting.journal.application.usecase.command;

import java.math.BigDecimal;

public record ManualJournalLineCommand(
        Long accountId,
        BigDecimal debitAmount,
        BigDecimal creditAmount,
        String description
) {
}
