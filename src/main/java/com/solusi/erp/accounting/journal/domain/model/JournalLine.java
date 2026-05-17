package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public record JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount,
                          Long originalCurrencyId, BigDecimal exchangeRate,
                          BigDecimal originalDebitAmount, BigDecimal originalCreditAmount) {
    public JournalLine {
        debitAmount = debitAmount == null ? BigDecimal.ZERO : debitAmount;
        creditAmount = creditAmount == null ? BigDecimal.ZERO : creditAmount;

        int debitSign = debitAmount.signum();
        int creditSign = creditAmount.signum();

        if (debitSign < 0 || creditSign < 0) {
            throw new DomainException("msg.error.journal.invalid.amount");
        }
        if ((debitSign > 0) == (creditSign > 0)) {
            throw new DomainException("msg.error.journal.invalid.line");
        }
    }

    public JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount) {
        this(accountId, debitAmount, creditAmount, null, null, null, null);
    }

    public static JournalLine debit(Long accountId, BigDecimal amount) {
        return new JournalLine(accountId, amount, BigDecimal.ZERO);
    }

    public static JournalLine credit(Long accountId, BigDecimal amount) {
        return new JournalLine(accountId, BigDecimal.ZERO, amount);
    }

    public static JournalLine debitWithOriginal(Long accountId, BigDecimal baseAmount,
                                                Long currencyId, BigDecimal rate, BigDecimal originalAmount) {
        return new JournalLine(accountId, baseAmount, BigDecimal.ZERO,
                currencyId, rate, originalAmount, BigDecimal.ZERO);
    }

    public static JournalLine creditWithOriginal(Long accountId, BigDecimal baseAmount,
                                                 Long currencyId, BigDecimal rate, BigDecimal originalAmount) {
        return new JournalLine(accountId, BigDecimal.ZERO, baseAmount,
                currencyId, rate, BigDecimal.ZERO, originalAmount);
    }
}
