package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountingSchemaTest {

    @Test
    void createNew_withValidLines() {
        List<AccountingSchemaLine> lines = List.of(
                new AccountingSchemaLine(null, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT),
                new AccountingSchemaLine(null, JournalVariable.GR_GRAND_TOTAL, 201L, JournalPosition.CREDIT)
        );

        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "Desc", true, lines);

        assertThat(schema.getLines()).hasSize(2);
    }

    @Test
    void createNew_throwsWhenLinesEmpty() {
        assertThatThrownBy(() -> AccountingSchema.createNew(SchemaEventType.GOODS_RECEIPT, "Desc", true, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.schema.lines.empty");
    }

    @Test
    void createNew_throwsWhenLinesNull() {
        assertThatThrownBy(() -> AccountingSchema.createNew(SchemaEventType.GOODS_RECEIPT, "Desc", true, null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.err.schema.lines.empty");
    }

    @Test
    void reconstitute_allowsPersistedSchemaWithoutLines() {
        AccountingSchema schema = AccountingSchema.reconstitute(
                new AuditMetadata(2L, 1L, null, null, null, null),
                SchemaEventType.VENDOR_BILL,
                "Vendor bill schema",
                true,
                List.of()
        );

        assertThat(schema.getId()).isEqualTo(2L);
        assertThat(schema.getEventType()).isEqualTo(SchemaEventType.VENDOR_BILL);
        assertThat(schema.getLines()).isEmpty();
    }

    @Test
    void update_changesLines() {
        AccountingSchema schema = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT,
                "Desc",
                true,
                List.of(new AccountingSchemaLine(null, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT))
        );

        schema.update(
                "New Desc",
                false,
                List.of(new AccountingSchemaLine(1L, JournalVariable.GR_GRAND_TOTAL, 201L, JournalPosition.CREDIT))
        );

        assertThat(schema.getDescription()).isEqualTo("New Desc");
        assertThat(schema.getIsActive()).isFalse();
        assertThat(schema.getLines()).hasSize(1);
        assertThat(schema.getLines().get(0).getAccountId()).isEqualTo(201L);
    }
}
