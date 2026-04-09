package com.solusi.erp.system.monitoring.infrastructure.config;

import com.solusi.erp.system.monitoring.application.service.MonitoringQueryService;
import com.solusi.erp.system.monitoring.domain.port.HealthCheckPort;
import com.solusi.erp.system.monitoring.domain.port.LogReaderPort;
import com.solusi.erp.system.monitoring.infrastructure.adapter.HealthCheckAdapter;
import com.solusi.erp.system.monitoring.infrastructure.adapter.LogReaderAdapter;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Composition Root for the System Monitoring module.
 * Wires all beans following Clean Architecture conventions.
 */
@Configuration
public class MonitoringConfig {

    @Bean
    public HealthCheckPort healthCheckPort(DataSource dataSource, MinioClient minioClient) {
        return new HealthCheckAdapter(dataSource, minioClient);
    }

    @Bean
    public LogReaderPort logReaderPort(@Value("${monitoring.log.directory:logs}") String logDirectory) {
        return new LogReaderAdapter(logDirectory);
    }

    @Bean
    public MonitoringQueryService monitoringQueryService(HealthCheckPort healthCheckPort, LogReaderPort logReaderPort) {
        return new MonitoringQueryService(healthCheckPort, logReaderPort);
    }
}
