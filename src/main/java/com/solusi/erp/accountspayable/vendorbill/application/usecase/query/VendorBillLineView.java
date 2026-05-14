package com.solusi.erp.accountspayable.vendorbill.application.usecase.query;

import java.math.BigDecimal;

public record VendorBillLineView(
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
        BigDecimal taxAmount,
        BigDecimal lineTotal
) {
}
