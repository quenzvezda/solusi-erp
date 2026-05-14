package com.solusi.erp.accountspayable.vendorbill.web.dto;

import com.solusi.erp.accountspayable.vendorbill.application.usecase.command.VendorBillLineCommand;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VendorBillSaveCommand(
        Long id,
        Long vendorId,
        String vendorInvoiceNumber,
        LocalDate billDate,
        LocalDate dueDate,
        Long currencyId,
        BigDecimal exchangeRate,
        String notes,
        List<Long> grIds,
        List<VendorBillLineCommand> lines
) {
}
