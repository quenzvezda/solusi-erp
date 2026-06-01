package com.solusi.erp.inventory.goodsissue.application.usecase.command;

import java.math.BigDecimal;

public record GoodsIssueLineCommand(
        Long id,
        Long referenceLineId,
        Long productId,
        Boolean serialized,
        BigDecimal quantityIssued,
        Long uomId,
        BigDecimal baseQuantity,
        Long facilityId,
        Long gridId,
        Long containerId,
        String serialNumber,
        BigDecimal unitCost,
        String valuationRefType,
        Long valuationRefId,
        Long valuationRefLineId
) {}
