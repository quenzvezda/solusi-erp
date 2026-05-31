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
    private Long currencyId;
    private String currencyName;
    private String currencyCode;
    private BigDecimal exchangeRate;
    private String referenceNo;
    private Long reversalOfId;
    private String reversalOfCode;
    private Long reversedById;
    private String reversedByCode;
    private String description;
    private String status;
    private List<JournalLineResponse> lines;
    private BigDecimal totalDebit;
    private BigDecimal totalCredit;
    private BigDecimal totalOriginalDebit;
    private BigDecimal totalOriginalCredit;
    private boolean hasMultiCurrencyLines;
    private boolean manual;
    private boolean reversal;
    private boolean reversed;
    private boolean multiCurrency;
}
