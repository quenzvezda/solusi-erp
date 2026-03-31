package com.solusi.erp.inventory.uom.domain.port;

/**
 * SPI: any slice that references a UnitOfMeasure implements this and registers it as a Spring bean.
 * UomInUseCheckerComposite aggregates all registered impls automatically.
 */
public interface UomUsageChecker {
    boolean isUsed(Long uomId);
}
