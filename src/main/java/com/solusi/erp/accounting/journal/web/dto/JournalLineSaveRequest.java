package com.solusi.erp.accounting.journal.web.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class JournalLineSaveRequest {
    private Long accountId;
    private String accountName;
    private String accountCode;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String description;
}
