package com.solusi.erp.accounting.schema.application.usecase.query;

import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.schema.web.dto.SchemaSimulationResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SimulateSchemaUseCaseImplTest {

    private final SimulateSchemaUseCaseImpl useCase = new SimulateSchemaUseCaseImpl();

    @Test
    void execute_returnsBalancedWhenDebitEqualsCredit() {
        List<AccountingSchemaLine> lines = List.of(
                new AccountingSchemaLine(1L, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT),
                new AccountingSchemaLine(2L, JournalVariable.GR_GRAND_TOTAL, 201L, JournalPosition.CREDIT)
        );
        Map<JournalVariable, BigDecimal> mockValues = Map.of(
                JournalVariable.GR_INVENTORY_AMT, new BigDecimal("100.00"),
                JournalVariable.GR_GRAND_TOTAL, new BigDecimal("100.00")
        );

        SchemaSimulationResponse response = useCase.execute(lines, mockValues);

        assertThat(response.isBalanced()).isTrue();
        assertThat(response.getTotalDebit()).isEqualByComparingTo("100.00");
        assertThat(response.getTotalCredit()).isEqualByComparingTo("100.00");
        assertThat(response.getMessage()).isEqualTo("Journal is balanced.");
    }

    @Test
    void execute_returnsUnbalancedWhenDebitNotEqualsCredit() {
        List<AccountingSchemaLine> lines = List.of(
                new AccountingSchemaLine(1L, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT),
                new AccountingSchemaLine(2L, JournalVariable.GR_GRAND_TOTAL, 201L, JournalPosition.CREDIT)
        );
        Map<JournalVariable, BigDecimal> mockValues = Map.of(
                JournalVariable.GR_INVENTORY_AMT, new BigDecimal("100.00"),
                JournalVariable.GR_GRAND_TOTAL, new BigDecimal("90.00")
        );

        SchemaSimulationResponse response = useCase.execute(lines, mockValues);

        assertThat(response.isBalanced()).isFalse();
        assertThat(response.getTotalDebit()).isEqualByComparingTo("100.00");
        assertThat(response.getTotalCredit()).isEqualByComparingTo("90.00");
        assertThat(response.getMessage()).isEqualTo("Journal is NOT balanced.");
    }

    @Test
    void execute_skipsZeroValuesAndNullValues() {
        List<AccountingSchemaLine> lines = List.of(
                new AccountingSchemaLine(1L, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT),
                new AccountingSchemaLine(2L, JournalVariable.GR_TAX_AMT, 301L, JournalPosition.DEBIT),
                new AccountingSchemaLine(3L, JournalVariable.GR_GRAND_TOTAL, 201L, JournalPosition.CREDIT)
        );
        Map<JournalVariable, BigDecimal> mockValues = Map.of(
                JournalVariable.GR_INVENTORY_AMT, new BigDecimal("100.00"),
                JournalVariable.GR_TAX_AMT, BigDecimal.ZERO, // This should be skipped
                JournalVariable.GR_GRAND_TOTAL, new BigDecimal("100.00")
                // Missing variable should be treated as ZERO
        );

        SchemaSimulationResponse response = useCase.execute(lines, mockValues);

        assertThat(response.isBalanced()).isTrue();
        assertThat(response.getTotalDebit()).isEqualByComparingTo("100.00");
        assertThat(response.getTotalCredit()).isEqualByComparingTo("100.00");
    }
}