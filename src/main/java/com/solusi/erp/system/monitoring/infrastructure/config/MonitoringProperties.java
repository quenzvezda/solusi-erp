package com.solusi.erp.system.monitoring.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds monitoring configuration from {@code monitoring.*} properties.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {

    private Log log = new Log();

    @Getter
    @Setter
    public static class Log {
        private String directory = "logs";
        private int maxLines = 500;
        private String timezone = "Asia/Jakarta";
    }
}
