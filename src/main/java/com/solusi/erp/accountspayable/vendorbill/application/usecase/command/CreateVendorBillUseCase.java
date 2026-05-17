package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FunctionalInterface
public interface CreateVendorBillUseCase {

    VendorBill execute(Long vendorId,
                       String vendorInvoiceNumber,
                       LocalDate billDate,
                       LocalDate dueDate,
                       Long currencyId,
                       BigDecimal exchangeRate,
                       String notes,
                       List<Long> grIds,
                       List<VendorBillLineCommand> lines);
}
