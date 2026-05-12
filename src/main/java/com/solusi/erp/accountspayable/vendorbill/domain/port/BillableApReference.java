package com.solusi.erp.accountspayable.vendorbill.domain.port;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillableApReference(
        String sourceType,
        Long sourceId,
        String sourceCode,
        LocalDate sourceDate,
        Long vendorId,
        String vendorName,
        Long currencyId,
        String currencyCode,
        BigDecimal exchangeRate,
        BigDecimal outstandingAmount,
        int outstandingLineCount,
        String status
) {
}
