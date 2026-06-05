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

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.VENDOR_BILL)).containsExactlyInAnyOrder(
                JournalVariable.VB_GRIR_CLEARING_AMT,
                JournalVariable.VB_TAX_AMT,
                JournalVariable.VB_AP_TOTAL,
                JournalVariable.VB_FX_LOSS_AMT,
                JournalVariable.VB_FX_GAIN_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.VENDOR_PAYMENT)).containsExactlyInAnyOrder(
                JournalVariable.VP_AP_AMT,
                JournalVariable.VP_BANK_OUT_AMT,
                JournalVariable.VP_FX_LOSS_AMT,
                JournalVariable.VP_FX_GAIN_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.CUSTOMER_INVOICE)).containsExactlyInAnyOrder(
                JournalVariable.CI_AR_AMT,
                JournalVariable.CI_REVENUE_AMT,
                JournalVariable.CI_TAX_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.GOODS_ISSUE)).containsExactlyInAnyOrder(
                JournalVariable.GI_COGS_AMT,
                JournalVariable.GI_INVENTORY_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.PURCHASE_RETURN)).containsExactlyInAnyOrder(
                JournalVariable.PR_GRIR_CLEARING_AMT,
                JournalVariable.PR_INVENTORY_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.DEBIT_MEMO_APPLICATION)).containsExactlyInAnyOrder(
                JournalVariable.DMA_AP_AMT,
                JournalVariable.DMA_GRIR_CLEARING_AMT,
                JournalVariable.DMA_TAX_AMT,
                JournalVariable.DMA_FX_LOSS_AMT,
                JournalVariable.DMA_FX_GAIN_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.CUSTOMER_RECEIPT)).containsExactlyInAnyOrder(
                JournalVariable.CR_BANK_IN_AMT,
                JournalVariable.CR_AR_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.STOCK_ADJUSTMENT_IN)).containsExactlyInAnyOrder(
                JournalVariable.SAI_INVENTORY_AMT,
                JournalVariable.SAI_GAIN_AMT
        );

        assertThat(JournalVariable.getVariablesForEvent(SchemaEventType.STOCK_ADJUSTMENT_OUT)).containsExactlyInAnyOrder(
                JournalVariable.SAO_LOSS_AMT,
                JournalVariable.SAO_INVENTORY_AMT
        );
    }
}
