package com.solusi.erp.accountspayable.vendorbill.domain.port;

import java.math.BigDecimal;

public record BillableGrLineView(
        Long grLineId,
        Long grId,
        Long productId,
        String productName,
        String productCode,
        BigDecimal quantityReceived,
        Long uomId,
        String uomName,
        BigDecimal unitPrice,
        BigDecimal inventoryAmount,
        BigDecimal taxAmount,
        BigDecimal grIrAmount,
        BigDecimal outstandingQty) {}
