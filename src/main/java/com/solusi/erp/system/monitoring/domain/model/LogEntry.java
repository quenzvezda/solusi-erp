package com.solusi.erp.system.monitoring.domain.model;

/**
 * Represents a single parsed log entry from the application log file.
 */
public record LogEntry(
        String timestamp,
        String level,
        String thread,
        String logger,
        String message,
        String stackTrace
) {
    public boolean isError() {
        return "ERROR".equals(level);
    }

    public boolean isWarn() {
        return "WARN".equals(level);
    }
}
