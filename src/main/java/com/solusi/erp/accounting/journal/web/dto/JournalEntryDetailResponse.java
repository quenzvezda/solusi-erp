package com.solusi.erp.accounting.journal.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class JournalEntryDetailResponse extends BaseAuditResponse {
    private String journalCode;
    private String eventType;
    private String sourceType;
    private Long sourceId;
    private String sourceCode;
    private LocalDate postingDate;
    private String description;
    private String status;
    private List<JournalLineResponse> lines;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
}
