package com.solusi.erp.accountspayable.vendorbill.application.usecase.command;

import java.math.BigDecimal;

public record VendorBillLineCommand(
        Long id,
        Long grLineId,
        Long productId,
        String productName,
        String description,
        BigDecimal qtyBilled,
        Long uomId,
        String uomName,
        BigDecimal unitPrice,
        BigDecimal inventoryAmount,
        BigDecimal taxAmount
) {
}
