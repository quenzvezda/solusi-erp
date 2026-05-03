package com.solusi.erp.accounting.schema.web.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SchemaSimulationResponse {
    private boolean balanced;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private String message;
}