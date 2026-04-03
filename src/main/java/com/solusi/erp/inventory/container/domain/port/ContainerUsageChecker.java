package com.solusi.erp.inventory.container.domain.port;

/**
 * SPI: any slice that references a Container implements this and registers it as a Spring bean.
 * ContainerInUseCheckerComposite aggregates all registered impls automatically.
 */
public interface ContainerUsageChecker {
    boolean isUsed(Long containerId);
}
