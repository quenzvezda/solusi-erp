package com.solusi.erp.purchasing.purchasereturn.application.usecase.query;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EligibleGoodsReceiptRow(
        Long goodsReceiptId,
        String goodsReceiptCode,
        Long purchaseOrderId,
        String purchaseOrderCode,
        Long supplierId,
        String supplierName,
        LocalDate receiptDate,
        Long facilityId,
        String facilityName,
        Long currencyId,
        String currencyCode,
        BigDecimal exchangeRate,
        long eligibleLineCount,
        BigDecimal totalReturnableQuantity
) {
}
