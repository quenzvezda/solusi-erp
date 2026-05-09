package com.solusi.erp.accounting.schema.application.usecase.query;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.web.dto.SchemaSimulationResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SimulateSchemaUseCase {
    SchemaSimulationResponse execute(List<AccountingSchemaLine> lines, Map<JournalVariable, BigDecimal> mockValues);
}