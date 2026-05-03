package com.solusi.erp.accounting.journal.web.dto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class JournalEntryListResponse {
    private Long id;
    private String journalCode;
    private String eventType;
    private String sourceType;
    private String sourceCode;
    private LocalDate postingDate;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private String status;
}