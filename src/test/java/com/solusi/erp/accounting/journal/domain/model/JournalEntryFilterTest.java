package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class JournalEntryFilterTest {
    @Test
    void filter_hasCorrectFields() {
        JournalEntryFilter filter = new JournalEntryFilter(
                SchemaEventType.GOODS_RECEIPT,
                "GR-001",
                "JNL-001",
                LocalDate.of(2026, 5, 1),
                LocalDate.of(2026, 5, 31)
        );
        assertThat(filter.sourceType()).isEqualTo(SchemaEventType.GOODS_RECEIPT);
        assertThat(filter.sourceCode()).isEqualTo("GR-001");
        assertThat(filter.postingDateFrom()).isEqualTo(LocalDate.of(2026, 5, 1));
    }
}