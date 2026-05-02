package com.solusi.erp.accounting.journal.application.policy;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoodsReceiptJournalPolicyTest {

    private final GoodsReceiptJournalPolicy policy = new GoodsReceiptJournalPolicy();

    @Test
    void buildLines_withoutTax_returnsTwoLines() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "GR", 101L, 201L, null, true);
        JournalPostingCommand cmd = command(new BigDecimal("500.0000"), BigDecimal.ZERO, new BigDecimal("500.0000"));

        List<JournalLine> lines = policy.buildLines(schema, cmd);

        assertThat(lines).hasSize(2);
        assertThat(lines.get(0).debitAmount()).isEqualByComparingTo("500.0000");
        assertThat(lines.get(1).creditAmount()).isEqualByComparingTo("500.0000");
    }

    @Test
    void buildLines_withTax_returnsThreeLines() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "GR", 101L, 201L, 301L, true);
        JournalPostingCommand cmd = command(new BigDecimal("500.0000"), new BigDecimal("50.0000"), new BigDecimal("550.0000"));

        List<JournalLine> lines = policy.buildLines(schema, cmd);

        assertThat(lines).hasSize(3);
        assertThat(lines.get(0).debitAmount()).isEqualByComparingTo("500.0000");
        assertThat(lines.get(1).debitAmount()).isEqualByComparingTo("50.0000");
        assertThat(lines.get(2).creditAmount()).isEqualByComparingTo("550.0000");
    }

    @Test
    void buildLines_throwsWhenTaxAmountPresentButNoTaxAccount() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "GR", 101L, 201L, null, true);
        JournalPostingCommand cmd = command(new BigDecimal("500.0000"), new BigDecimal("50.0000"), new BigDecimal("550.0000"));

        assertThatThrownBy(() -> policy.buildLines(schema, cmd))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.schema.tax.account.required");
    }

    private JournalPostingCommand command(BigDecimal inventory, BigDecimal tax, BigDecimal total) {
        return new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 6L, "GR-0006",
                LocalDate.now(), "Auto journal", inventory, tax, total);
    }
}
