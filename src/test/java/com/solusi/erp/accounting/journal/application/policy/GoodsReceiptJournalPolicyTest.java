package com.solusi.erp.accounting.journal.application.policy;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
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
    void supports_returnsGoodsReceipt() {
        assertThat(policy.supports()).isEqualTo(SchemaEventType.GOODS_RECEIPT);
    }

    @Test
    void buildLines_returnsCorrectLines() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "GR", true,
                List.of(
                        new AccountingSchemaLine(1L, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT),
                        new AccountingSchemaLine(2L, JournalVariable.GR_TAX_AMT, 301L, JournalPosition.DEBIT),
                        new AccountingSchemaLine(3L, JournalVariable.GR_GRAND_TOTAL, 201L, JournalPosition.CREDIT)
                )
        );

        JournalPostingCommand cmd = new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 1L, "GR-001",
                LocalDate.now(), "Desc",
                new BigDecimal("100.00"), new BigDecimal("11.00"), new BigDecimal("111.00")
        );

        List<JournalLine> lines = policy.buildLines(schema, cmd);

        assertThat(lines).hasSize(3);
        assertThat(lines).extracting(JournalLine::accountId).containsExactly(101L, 301L, 201L);
        assertThat(lines.get(0).debitAmount()).isEqualByComparingTo("100.00");
        assertThat(lines.get(1).debitAmount()).isEqualByComparingTo("11.00");
        assertThat(lines.get(2).creditAmount()).isEqualByComparingTo("111.00");
    }

    @Test
    void buildLines_skipsZeroAmountLines() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "GR", true,
                List.of(
                        new AccountingSchemaLine(1L, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT),
                        new AccountingSchemaLine(2L, JournalVariable.GR_TAX_AMT, 301L, JournalPosition.DEBIT),
                        new AccountingSchemaLine(3L, JournalVariable.GR_GRAND_TOTAL, 201L, JournalPosition.CREDIT)
                )
        );

        JournalPostingCommand cmd = new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 1L, "GR-001",
                LocalDate.now(), "Desc",
                new BigDecimal("100.00"), BigDecimal.ZERO, new BigDecimal("100.00") // Zero tax
        );

        List<JournalLine> lines = policy.buildLines(schema, cmd);

        assertThat(lines).hasSize(2);
        assertThat(lines).extracting(JournalLine::accountId).containsExactly(101L, 201L);
        assertThat(lines.get(0).debitAmount()).isEqualByComparingTo("100.00");
        assertThat(lines.get(1).creditAmount()).isEqualByComparingTo("100.00");
    }
}