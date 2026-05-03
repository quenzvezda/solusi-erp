package com.solusi.erp.accounting.schema.application.usecase.query;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.web.dto.SchemaSimulationResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SimulateSchemaUseCaseImpl implements SimulateSchemaUseCase {
    @Override
    public SchemaSimulationResponse execute(List<AccountingSchemaLine> lines, Map<JournalVariable, BigDecimal> mockValues) {
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;

        for (AccountingSchemaLine line : lines) {
            BigDecimal val = mockValues.getOrDefault(line.variable(), BigDecimal.ZERO);
            if (val == null || val.compareTo(BigDecimal.ZERO) == 0) continue; // Zero skipping logic

            if (line.position() == JournalPosition.DEBIT) {
                totalDebit = totalDebit.add(val);
            } else {
                totalCredit = totalCredit.add(val);
            }
        }

        boolean balanced = totalDebit.compareTo(totalCredit) == 0;
        String msg = balanced ? "Journal is balanced." : "Journal is NOT balanced.";
        return new SchemaSimulationResponse(balanced, totalDebit, totalCredit, msg);
    }
}