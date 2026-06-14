package com.solusi.erp.inventory.stock.domain.model;

import java.math.BigDecimal;

public record InventoryReservationRequest(
        Long productId,
        Long facilityId,
        Long gridId,
        Long containerId,
        boolean serialized,
        String serialNumber,
        ReferenceType valuationReferenceType,
        Long valuationReferenceId,
        Long valuationReferenceLineId,
        BigDecimal quantity
) {
}
