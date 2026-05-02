package com.solusi.erp.accounting.journal.domain.model;

import java.math.BigDecimal;

public record JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount) {

    public static JournalLine debit(Long accountId, BigDecimal amount) {
        return new JournalLine(accountId, amount, BigDecimal.ZERO);
    }

    public static JournalLine credit(Long accountId, BigDecimal amount) {
        return new JournalLine(accountId, BigDecimal.ZERO, amount);
    }
}
