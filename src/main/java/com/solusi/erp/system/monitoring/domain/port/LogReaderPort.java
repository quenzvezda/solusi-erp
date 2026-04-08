package com.solusi.erp.system.monitoring.domain.port;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;

import java.io.InputStream;
import java.util.List;

/**
 * Port for reading application log entries.
 */
public interface LogReaderPort {
    List<LogEntry> readRecentLogs(int limit, String levelFilter, String keyword);
    InputStream getLogFileStream(boolean errorOnly);
    long getLogFileSize(boolean errorOnly);
}
