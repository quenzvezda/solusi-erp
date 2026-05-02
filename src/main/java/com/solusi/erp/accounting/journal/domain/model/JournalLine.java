package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public record JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount) {
    public JournalLine {
        debitAmount = debitAmount == null ? BigDecimal.ZERO : debitAmount;
        creditAmount = creditAmount == null ? BigDecimal.ZERO : creditAmount;

        if (debitAmount.signum() < 0 || creditAmount.signum() < 0) {
            throw new DomainException("msg.error.journal.invalid.amount");
        }
        if (debitAmount.signum() > 0 && creditAmount.signum() > 0) {
            throw new DomainException("msg.error.journal.invalid.line");
        }
    }

    public static JournalLine debit(Long accountId, BigDecimal amount) {
        return new JournalLine(accountId, amount, BigDecimal.ZERO);
    }

    public static JournalLine credit(Long accountId, BigDecimal amount) {
        return new JournalLine(accountId, BigDecimal.ZERO, amount);
    }
}
