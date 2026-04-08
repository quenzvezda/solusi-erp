package com.solusi.erp.system.monitoring.domain.model;

/**
 * Represents the health status of a monitored service.
 */
public record ServiceHealth(
        String name,
        Status status,
        String detail
) {
    public enum Status {
        UP, DOWN, UNKNOWN
    }

    public static ServiceHealth up(String name, String detail) {
        return new ServiceHealth(name, Status.UP, detail);
    }

    public static ServiceHealth down(String name, String detail) {
        return new ServiceHealth(name, Status.DOWN, detail);
    }

    public static ServiceHealth unknown(String name, String detail) {
        return new ServiceHealth(name, Status.UNKNOWN, detail);
    }
}
