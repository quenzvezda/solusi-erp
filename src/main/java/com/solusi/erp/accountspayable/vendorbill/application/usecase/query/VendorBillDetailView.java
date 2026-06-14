package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillDocumentStatus;
import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillSettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VendorBillDetailView(
        Long id,
        String code,
        Long vendorId,
        String vendorInvoiceNumber,
        LocalDate billDate,
        LocalDate dueDate,
        Long currencyId,
        BigDecimal exchangeRate,
        VendorBillDocumentStatus documentStatus,
        VendorBillSettlementStatus settlementStatus,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal debitMemoAppliedAmount,
        BigDecimal outstandingAmount,
        String notes,
        List<Long> grIds,
        List<VendorBillLineView> lines
) {
}
