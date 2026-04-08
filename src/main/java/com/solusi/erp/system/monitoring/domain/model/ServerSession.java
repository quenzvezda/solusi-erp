package com.solusi.erp.system.monitoring.domain.model;

/**
 * Represents one server session — the period between a server start and the next restart (or now).
 *
 * @param id       ordinal index (1-based, newest first)
 * @param startedAt formatted start timestamp (WIB)
 * @param endedAt   formatted end timestamp (WIB), or null if this is the current session
 * @param current   true if this is the currently running session
 * @param label     human-readable label for UI display
 */
public record ServerSession(
        int id,
        String startedAt,
        String endedAt,
        boolean current,
        String label
) {}
