package com.solusi.erp.system.monitoring.application.service;

import com.solusi.erp.system.monitoring.domain.model.LogEntry;
import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import com.solusi.erp.system.monitoring.domain.port.HealthCheckPort;
import com.solusi.erp.system.monitoring.domain.port.LogReaderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("MonitoringQueryService — Unit Test")
class MonitoringQueryServiceTest {

    private HealthCheckPort healthCheckPort;
    private LogReaderPort logReaderPort;
    private MonitoringQueryService service;

    @BeforeEach
    void setUp() {
        healthCheckPort = mock(HealthCheckPort.class);
        logReaderPort = mock(LogReaderPort.class);
        service = new MonitoringQueryService(healthCheckPort, logReaderPort);
    }

    @Test
    @DisplayName("getSystemHealth aggregates services and JVM metrics from port")
    void getSystemHealth_aggregatesFromPort() {
        List<ServiceHealth> services = List.of(
                ServiceHealth.up("database", "Ping: 2ms"),
                ServiceHealth.up("minio", "Latency: 5ms"),
                ServiceHealth.down("disk", "0 GB free")
        );
        SystemHealthSnapshot.JvmMetrics jvm = new SystemHealthSnapshot.JvmMetrics("2h 30m", 256, 512, 50, 25);

        when(healthCheckPort.checkAll()).thenReturn(services);
        when(healthCheckPort.getJvmMetrics()).thenReturn(jvm);

        SystemHealthSnapshot snapshot = service.getSystemHealth();

        assertThat(snapshot.services()).hasSize(3);
        assertThat(snapshot.services().get(0).name()).isEqualTo("database");
        assertThat(snapshot.services().get(2).status()).isEqualTo(ServiceHealth.Status.DOWN);
        assertThat(snapshot.jvm().uptime()).isEqualTo("2h 30m");
        assertThat(snapshot.jvm().memoryPercent()).isEqualTo(50);

        verify(healthCheckPort).checkAll();
        verify(healthCheckPort).getJvmMetrics();
    }

    @Test
    @DisplayName("getRecentLogs delegates to LogReaderPort with correct parameters")
    void getRecentLogs_delegatesToPort() {
        List<LogEntry> expected = List.of(
                new LogEntry("2026-04-08 10:00:00", "INFO", "main", "c.s.e.App", "Started", null),
                new LogEntry("2026-04-08 10:00:01", "ERROR", "main", "c.s.e.App", "Failure", "NullPointerException")
        );
        when(logReaderPort.readRecentLogs(50, "ERROR", "fail")).thenReturn(expected);

        List<LogEntry> result = service.getRecentLogs(50, "ERROR", "fail");

        assertThat(result).hasSize(2);
        assertThat(result.get(1).level()).isEqualTo("ERROR");
        verify(logReaderPort).readRecentLogs(50, "ERROR", "fail");
    }

    @Test
    @DisplayName("getLogFileStream delegates errorOnly flag to port")
    void getLogFileStream_delegatesToPort() {
        InputStream expected = new ByteArrayInputStream("log content".getBytes());
        when(logReaderPort.getLogFileStream(true)).thenReturn(expected);

        InputStream result = service.getLogFileStream(true);

        assertThat(result).isSameAs(expected);
        verify(logReaderPort).getLogFileStream(true);
    }

    @Test
    @DisplayName("getLogFileSize delegates errorOnly flag to port")
    void getLogFileSize_delegatesToPort() {
        when(logReaderPort.getLogFileSize(false)).thenReturn(1024L);

        long result = service.getLogFileSize(false);

        assertThat(result).isEqualTo(1024L);
        verify(logReaderPort).getLogFileSize(false);
    }
}
