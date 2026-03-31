package com.solusi.erp.inventory.grid.domain.port;

/**
 * Cross-slice usage checker port for Grid.
 * Any slice that references a Grid must implement this interface
 * and register it as a Spring bean so GridInUseCheckerComposite
 * can aggregate all checks automatically.
 */
public interface GridUsageChecker {
    boolean isUsed(Long gridId);
}
