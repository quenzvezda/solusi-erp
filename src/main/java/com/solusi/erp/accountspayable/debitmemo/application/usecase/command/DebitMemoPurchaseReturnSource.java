package com.solusi.erp.accountspayable.debitmemo.application.usecase.command;

import java.time.LocalDate;
import java.util.List;

public record DebitMemoPurchaseReturnSource(
        Long purchaseReturnId,
        String purchaseReturnCode,
        Long vendorId,
        Long currencyId,
        LocalDate returnDate,
        List<DebitMemoPurchaseReturnLineSource> lines
) {
}

