package com.solusi.erp.system.monitoring.domain.model;

import java.util.List;

/**
 * Holds filtered log entries plus aggregate counts (computed before applying the limit).
 */
public record LogViewResult(
        List<LogEntry> entries,
        long totalMatched,
        long errorCount,
        long warnCount,
        long infoCount,
        long debugCount
) {}
