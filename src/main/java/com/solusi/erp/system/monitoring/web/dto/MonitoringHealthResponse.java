package com.solusi.erp.system.monitoring.web.dto;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Response DTO for the monitoring health API.
 */
@Getter
@Builder
public class MonitoringHealthResponse {
    private final List<ServiceHealthDto> services;
    private final JvmMetricsDto jvm;
    private final String timestamp;

    @Getter
    @Builder
    public static class ServiceHealthDto {
        private final String name;
        private final String status;
        private final String detail;
        private final String badgeClass;

        public static ServiceHealthDto from(ServiceHealth health) {
            String badge = switch (health.status()) {
                case UP -> "bg-success-lt";
                case DOWN -> "bg-danger-lt";
                case UNKNOWN -> "bg-secondary-lt";
            };
            return ServiceHealthDto.builder()
                    .name(health.name())
                    .status(health.status().name())
                    .detail(health.detail())
                    .badgeClass(badge)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class JvmMetricsDto {
        private final String uptime;
        private final long usedMemoryMb;
        private final long maxMemoryMb;
        private final int memoryPercent;
        private final int activeThreads;
        private final String progressBarClass;

        public static JvmMetricsDto from(SystemHealthSnapshot.JvmMetrics jvm) {
            String progressClass = jvm.memoryPercent() > 85 ? "bg-danger"
                    : jvm.memoryPercent() > 70 ? "bg-warning"
                    : "bg-success";
            return JvmMetricsDto.builder()
                    .uptime(jvm.uptime())
                    .usedMemoryMb(jvm.usedMemoryMb())
                    .maxMemoryMb(jvm.maxMemoryMb())
                    .memoryPercent(jvm.memoryPercent())
                    .activeThreads(jvm.activeThreads())
                    .progressBarClass(progressClass)
                    .build();
        }
    }

    public static MonitoringHealthResponse from(SystemHealthSnapshot snapshot, String timestamp) {
        return MonitoringHealthResponse.builder()
                .services(snapshot.services().stream().map(ServiceHealthDto::from).toList())
                .jvm(JvmMetricsDto.from(snapshot.jvm()))
                .timestamp(timestamp)
                .build();
    }
}
