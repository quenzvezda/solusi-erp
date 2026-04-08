package com.solusi.erp.system.monitoring.domain.port;

import com.solusi.erp.system.monitoring.domain.model.LogViewResult;
import com.solusi.erp.system.monitoring.domain.model.ServerSession;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Port for reading application log entries.
 */
public interface LogReaderPort {
    LogViewResult readRecentLogs(int limit, Set<String> levelFilters, String keyword, Integer sessionId, String timeRange);
    InputStream getLogFileStream(boolean errorOnly);
    long getLogFileSize(boolean errorOnly);
    List<ServerSession> detectSessions();
}
