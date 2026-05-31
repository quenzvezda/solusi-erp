package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator.CurrencyPostingInfo;

import java.math.BigDecimal;
import java.util.List;

class ManualJournalCommandValidator {

    private final CoaPostingValidator coaPostingValidator;
    private final CurrencyPostingValidator currencyPostingValidator;

    ManualJournalCommandValidator(CoaPostingValidator coaPostingValidator,
                                  CurrencyPostingValidator currencyPostingValidator) {
        this.coaPostingValidator = coaPostingValidator;
        this.currencyPostingValidator = currencyPostingValidator;
    }

    List<JournalLine> buildLines(ManualJournalCommand command) {
        validateCurrency(command);
        if (command.lines() == null) {
            throw new DomainException("msg.error.journal.lines.required");
        }
        return command.lines().stream()
                .map(line -> toJournalLine(command, line))
                .toList();
    }

    void validateEntry(JournalEntry entry) {
        validateCurrency(entry.getCurrencyId(), entry.getExchangeRate());
        for (JournalLine line : entry.getLines()) {
            if (!coaPostingValidator.isPostable(line.accountId())) {
                throw new DomainException("msg.error.journal.account.invalid");
            }
        }
    }

    private void validateCurrency(ManualJournalCommand command) {
        validateCurrency(command.currencyId(), command.exchangeRate());
    }

    private void validateCurrency(Long currencyId, BigDecimal exchangeRate) {
        if (exchangeRate == null || exchangeRate.signum() <= 0) {
            throw new DomainException("msg.error.journal.exchange.rate.required");
        }
        CurrencyPostingInfo currency = currencyPostingValidator.getPostingInfo(currencyId);
        if (currency == null || !currency.active()) {
            throw new DomainException("msg.error.journal.currency.invalid");
        }
        if (currency.defaultCurrency() && exchangeRate.compareTo(BigDecimal.ONE) != 0) {
            throw new DomainException("msg.error.journal.default.currency.rate.invalid");
        }
    }

    private JournalLine toJournalLine(ManualJournalCommand command, ManualJournalLineCommand line) {
        if (!coaPostingValidator.isPostable(line.accountId())) {
            throw new DomainException("msg.error.journal.account.invalid");
        }

        BigDecimal debit = line.debitAmount() == null ? BigDecimal.ZERO : line.debitAmount();
        BigDecimal credit = line.creditAmount() == null ? BigDecimal.ZERO : line.creditAmount();
        if (debit.signum() > 0 && credit.signum() == 0) {
            return JournalLine.manualDebit(line.accountId(), debit, command.currencyId(), command.exchangeRate(), line.description());
        }
        if (credit.signum() > 0 && debit.signum() == 0) {
            return JournalLine.manualCredit(line.accountId(), credit, command.currencyId(), command.exchangeRate(), line.description());
        }
        throw new DomainException("msg.error.journal.invalid.line");
    }
}
