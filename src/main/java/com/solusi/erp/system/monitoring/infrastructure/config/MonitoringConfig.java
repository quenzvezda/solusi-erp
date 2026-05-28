package com.solusi.erp.system.monitoring.infrastructure.config;

import com.solusi.erp.system.monitoring.application.service.MonitoringQueryService;
import com.solusi.erp.system.monitoring.domain.port.HealthCheckPort;
import com.solusi.erp.system.monitoring.domain.port.LogReaderPort;
import com.solusi.erp.system.monitoring.infrastructure.adapter.HealthCheckAdapter;
import com.solusi.erp.system.monitoring.infrastructure.adapter.LogReaderAdapter;
import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Composition Root for the System Monitoring module.
 * Wires all beans following Clean Architecture conventions.
 */
@Configuration
@EnableConfigurationProperties(MonitoringProperties.class)
public class MonitoringConfig {

    @Bean
    public HealthCheckPort healthCheckPort(DataSource dataSource, MinioClient minioClient) {
        return new HealthCheckAdapter(dataSource, minioClient);
    }

    @Bean
    public LogReaderPort logReaderPort(MonitoringProperties properties) {
        return new LogReaderAdapter(properties.getLog().getDirectory());
    }

    @Bean
    public MonitoringQueryService monitoringQueryService(HealthCheckPort healthCheckPort, LogReaderPort logReaderPort) {
        return new MonitoringQueryService(healthCheckPort, logReaderPort);
    }
}
