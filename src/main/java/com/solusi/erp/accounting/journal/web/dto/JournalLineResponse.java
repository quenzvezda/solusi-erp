package com.solusi.erp.accounting.journal.web.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalLineResponse {
    private Long accountId;
    private String accountCode;
    private String accountName;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String originalCurrencyCode;
    private BigDecimal exchangeRate;
    private BigDecimal originalDebitAmount;
    private BigDecimal originalCreditAmount;
    private String description;
}
