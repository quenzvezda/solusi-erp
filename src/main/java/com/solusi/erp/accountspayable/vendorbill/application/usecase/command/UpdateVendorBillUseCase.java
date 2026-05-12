package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import com.solusi.erp.accountspayable.vendorbill.domain.model.VendorBill;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface UpdateVendorBillUseCase {

    VendorBill execute(Long id,
                       Long vendorId,
                       String vendorInvoiceNumber,
                       LocalDate billDate,
                       LocalDate dueDate,
                       Long currencyId,
                       BigDecimal exchangeRate,
                       String notes,
                       List<Long> grIds,
                       List<VendorBillLineCommand> lines);
}
