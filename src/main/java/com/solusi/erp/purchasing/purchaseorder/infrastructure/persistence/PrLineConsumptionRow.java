package com.solusi.erp.purchasing.purchaseorder.infrastructure.persistence;

import java.math.BigDecimal;

public record PrLineConsumptionRow(
        Long prLineId,
        BigDecimal consumedQuantity
) {
}
