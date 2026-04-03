package com.solusi.erp.inventory.adjustment.application.usecase.command;

import java.math.BigDecimal;

/**
 * Command record representing a single line in a stock adjustment.
 */
public record LineCommand(
        Long id,
        Integer version,
        Long productId,
        String productCode,
        String productName,
        Boolean isSerialized,
        Long gridId,
        String gridCode,
        String gridName,
        Long containerId,
        String containerCode,
        String containerName,
        String facilityName,
        Long uomId,
        String uomName,
        BigDecimal conversionFactor,
        BigDecimal quantity,
        BigDecimal unitCost,
        String serialNumber
) {}
