package com.solusi.erp.accounting.journal.application.policy;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GoodsReceiptJournalPolicy implements JournalPolicy {

    @Override
    public SchemaEventType supports() {
        return SchemaEventType.GOODS_RECEIPT;
    }

    @Override
    public List<JournalLine> buildLines(AccountingSchema schema, JournalPostingCommand cmd) {
        boolean hasTax = cmd.taxAmount() != null && cmd.taxAmount().compareTo(BigDecimal.ZERO) > 0;
        if (hasTax && schema.getTaxAccountId() == null) {
            throw new DomainException("msg.error.journal.schema.tax.account.required");
        }

        List<JournalLine> lines = new ArrayList<>();
        lines.add(JournalLine.debit(schema.getDebitAccountId(), cmd.inventoryAmount()));
        if (hasTax) {
            lines.add(JournalLine.debit(schema.getTaxAccountId(), cmd.taxAmount()));
        }
        lines.add(JournalLine.credit(schema.getCreditAccountId(), cmd.totalAmount()));
        return lines;
    }
}
