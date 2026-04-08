package com.solusi.erp.system.monitoring.domain.model;

import java.util.List;

/**
 * Aggregated system health snapshot containing all service statuses and JVM metrics.
 */
public record SystemHealthSnapshot(
        List<ServiceHealth> services,
        JvmMetrics jvm
) {
    public record JvmMetrics(
            String uptime,
            long usedMemoryMb,
            long maxMemoryMb,
            int memoryPercent,
            int activeThreads
    ) {}
}
