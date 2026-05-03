package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class JournalVariableTest {
    @Test
    void getVariablesForEvent_returnsCorrectVariables() {
        List<JournalVariable> grVars = JournalVariable.getVariablesForEvent(SchemaEventType.GOODS_RECEIPT);
        assertThat(grVars).containsExactlyInAnyOrder(
                JournalVariable.GR_INVENTORY_AMT,
                JournalVariable.GR_TAX_AMT,
                JournalVariable.GR_GRAND_TOTAL
        );
        
        List<JournalVariable> otherVars = JournalVariable.getVariablesForEvent(SchemaEventType.VENDOR_BILL);
        assertThat(otherVars).isEmpty();
    }
}