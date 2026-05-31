package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public record JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount,
                          Long originalCurrencyId, BigDecimal exchangeRate,
                          BigDecimal originalDebitAmount, BigDecimal originalCreditAmount,
                          String description) {
    public JournalLine {
        if (accountId == null) {
            throw new DomainException("msg.error.journal.account.required");
        }

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
        if ((originalDebitAmount != null && originalDebitAmount.signum() < 0)
                || (originalCreditAmount != null && originalCreditAmount.signum() < 0)) {
            throw new DomainException("msg.error.journal.invalid.amount");
        }
    }

    public JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount,
                       Long originalCurrencyId, BigDecimal exchangeRate,
                       BigDecimal originalDebitAmount, BigDecimal originalCreditAmount) {
        this(accountId, debitAmount, creditAmount, originalCurrencyId, exchangeRate,
                originalDebitAmount, originalCreditAmount, null);
    }

    public JournalLine(Long accountId, BigDecimal debitAmount, BigDecimal creditAmount) {
        this(accountId, debitAmount, creditAmount, null, null, null, null, null);
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
                currencyId, rate, originalAmount, BigDecimal.ZERO, null);
    }

    public static JournalLine creditWithOriginal(Long accountId, BigDecimal baseAmount,
                                                 Long currencyId, BigDecimal rate, BigDecimal originalAmount) {
        return new JournalLine(accountId, BigDecimal.ZERO, baseAmount,
                currencyId, rate, BigDecimal.ZERO, originalAmount, null);
    }

    public static JournalLine manualDebit(Long accountId, BigDecimal transactionAmount,
                                          Long currencyId, BigDecimal exchangeRate, String description) {
        BigDecimal baseAmount = toBaseAmount(transactionAmount, exchangeRate);
        return new JournalLine(accountId, baseAmount, BigDecimal.ZERO,
                currencyId, exchangeRate, transactionAmount, BigDecimal.ZERO, description);
    }

    public static JournalLine manualCredit(Long accountId, BigDecimal transactionAmount,
                                           Long currencyId, BigDecimal exchangeRate, String description) {
        BigDecimal baseAmount = toBaseAmount(transactionAmount, exchangeRate);
        return new JournalLine(accountId, BigDecimal.ZERO, baseAmount,
                currencyId, exchangeRate, BigDecimal.ZERO, transactionAmount, description);
    }

    JournalLine reversed() {
        return new JournalLine(accountId, creditAmount, debitAmount,
                originalCurrencyId, exchangeRate, originalCreditAmount, originalDebitAmount, description);
    }

    private static BigDecimal toBaseAmount(BigDecimal transactionAmount, BigDecimal exchangeRate) {
        if (transactionAmount == null || exchangeRate == null) {
            throw new DomainException("msg.error.journal.invalid.amount");
        }
        return transactionAmount.multiply(exchangeRate);
    }
}
