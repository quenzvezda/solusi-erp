package com.solusi.erp.system.monitoring.application.service;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import com.solusi.erp.system.monitoring.domain.port.HealthCheckPort;
import com.solusi.erp.system.monitoring.domain.port.LogReaderPort;

import java.io.InputStream;
import java.util.List;

/**
 * Application service that orchestrates monitoring queries.
 * Pure Java — no Spring annotations.
 */
public class MonitoringQueryService {

    private final HealthCheckPort healthCheckPort;
    private final LogReaderPort logReaderPort;

    public MonitoringQueryService(HealthCheckPort healthCheckPort, LogReaderPort logReaderPort) {
        this.healthCheckPort = healthCheckPort;
        this.logReaderPort = logReaderPort;
    }

    public SystemHealthSnapshot getSystemHealth() {
        List<ServiceHealth> services = healthCheckPort.checkAll();
        SystemHealthSnapshot.JvmMetrics jvm = healthCheckPort.getJvmMetrics();
        return new SystemHealthSnapshot(services, jvm);
    }

    public List<LogEntry> getRecentLogs(int limit, String levelFilter, String keyword) {
        return logReaderPort.readRecentLogs(limit, levelFilter, keyword);
    }

    public InputStream getLogFileStream(boolean errorOnly) {
        return logReaderPort.getLogFileStream(errorOnly);
    }

    public long getLogFileSize(boolean errorOnly) {
        return logReaderPort.getLogFileSize(errorOnly);
    }
}
