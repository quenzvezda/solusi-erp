package com.solusi.erp.accounting.journal.web.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class JournalLineResponse {
    private Long accountId;
    private String accountCode;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
}