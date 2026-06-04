package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VendorBillSummaryView(
        Long id,
        String code,
        Long vendorId,
        String vendorInvoiceNumber,
        LocalDate billDate,
        LocalDate dueDate,
        VendorBillDocumentStatus status,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount
) {
}
