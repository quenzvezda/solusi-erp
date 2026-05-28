package com.solusi.erp.system.monitoring.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MonitoringPropertiesTest {

    @Test
    @DisplayName("Monitoring properties bind log configuration")
    void monitoringPropertiesBindLogConfiguration() {
        MapConfigurationPropertySource source = new MapConfigurationPropertySource(Map.of(
                "monitoring.log.directory", "/var/log/erp",
                "monitoring.log.max-lines", "250",
                "monitoring.log.timezone", "Asia/Jakarta"
        ));

        MonitoringProperties properties = new Binder(source)
                .bind("monitoring", MonitoringProperties.class)
                .orElseThrow(() -> new AssertionError("monitoring properties should bind"));

        assertThat(properties.getLog().getDirectory()).isEqualTo("/var/log/erp");
        assertThat(properties.getLog().getMaxLines()).isEqualTo(250);
        assertThat(properties.getLog().getTimezone()).isEqualTo("Asia/Jakarta");
    }
}
