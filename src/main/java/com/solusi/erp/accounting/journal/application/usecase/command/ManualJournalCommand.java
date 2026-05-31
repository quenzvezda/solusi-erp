package com.solusi.erp.accounting.journal.application.usecase.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ManualJournalCommand(
        LocalDate postingDate,
        Long currencyId,
        BigDecimal exchangeRate,
        String referenceNo,
        String description,
        List<ManualJournalLineCommand> lines
) {
}
