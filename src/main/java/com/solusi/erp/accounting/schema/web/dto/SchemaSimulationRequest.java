package com.solusi.erp.accounting.schema.web.dto;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;

@Data
public class SchemaSimulationRequest {
    private String eventType;
    private List<SchemaSaveRequest.SchemaLineRequest> lines;
    private Map<JournalVariable, BigDecimal> mockValues;
}