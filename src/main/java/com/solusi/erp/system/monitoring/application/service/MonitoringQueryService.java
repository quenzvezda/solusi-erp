package com.solusi.erp.system.monitoring.application.service;

import com.solusi.erp.system.monitoring.domain.model.LogViewResult;
import com.solusi.erp.system.monitoring.domain.model.ServerSession;
import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import com.solusi.erp.system.monitoring.domain.port.HealthCheckPort;
import com.solusi.erp.system.monitoring.domain.port.LogReaderPort;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

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

    public LogViewResult getRecentLogs(int limit, Set<String> levelFilters, String keyword, Integer sessionId, String timeRange, String dateFrom, String dateTo) {
        return logReaderPort.readRecentLogs(limit, levelFilters, keyword, sessionId, timeRange, dateFrom, dateTo);
    }

    public List<ServerSession> getServerSessions() {
        return logReaderPort.detectSessions();
    }

    public InputStream getLogFileStream(boolean errorOnly) {
        return logReaderPort.getLogFileStream(errorOnly);
    }

    public long getLogFileSize(boolean errorOnly) {
        return logReaderPort.getLogFileSize(errorOnly);
    }

    public void clearLog() {
        logReaderPort.clearLog();
    }

    public InputStream getAllLogsZipStream() {
        return logReaderPort.getAllLogsZipStream();
    }
}
