package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import java.time.LocalDate;

public record PurchaseOrderPrSelectorRow(
        Long prId,
        String prCode,
        LocalDate requestDate,
        Long supplierId,
        String supplierName,
        String supplierSubtext,
        Long facilityId,
        String facilityName,
        String facilitySubtext,
        Long currencyId,
        String currencyName,
        String currencySubtext,
        long remainingLineCount
) {
}
