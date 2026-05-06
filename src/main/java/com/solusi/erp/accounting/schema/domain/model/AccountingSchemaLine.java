package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;

public final class AccountingSchemaLine {
    private final Long id;
    private final JournalVariable var;
    private final Long accountId;
    private final JournalPosition position;

    public AccountingSchemaLine(Long id, JournalVariable var, Long accountId, JournalPosition position) {
        this.id = id;
        this.var = var;
        this.accountId = accountId;
        this.position = position;
    }

    public Long id() {
        return id;
    }

    public JournalVariable var() {
        return var;
    }

    public Long accountId() {
        return accountId;
    }

    public JournalPosition position() {
        return position;
    }

    public Long getId() {
        return id;
    }

    public JournalVariable getVar() {
        return var;
    }

    public Long getAccountId() {
        return accountId;
    }

    public JournalPosition getPosition() {
        return position;
    }
}
