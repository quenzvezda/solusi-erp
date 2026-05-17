package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBillStatus;

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
        VendorBillStatus status,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal outstandingAmount,
        String notes,
        List<Long> grIds,
        List<VendorBillLineView> lines
) {
}
