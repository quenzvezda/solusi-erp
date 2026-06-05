package com.solusi.erp.accountspayable.debitmemoallocation.application.usecase.command;

import java.math.BigDecimal;

public record DebitMemoAllocationLineCommand(
        Long vendorBillId,
        BigDecimal appliedGrossOriginal
) {
}
