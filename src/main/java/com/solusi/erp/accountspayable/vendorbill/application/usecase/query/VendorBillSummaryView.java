package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record VendorBillSummaryView(
        Long id,
        String code,
        Long vendorId,
        String vendorInvoiceNumber,
        LocalDate billDate,
        LocalDate dueDate,
        VendorBillDocumentStatus documentStatus,
        VendorBillSettlementStatus settlementStatus,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal debitMemoAppliedAmount,
        BigDecimal outstandingAmount
) {
}
