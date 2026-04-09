package com.solusi.erp.system.monitoring.infrastructure.adapter;

import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;
import com.solusi.erp.system.monitoring.domain.port.HealthCheckPort;
import io.minio.ListBucketsArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Infrastructure adapter that checks health of all external services.
 */
public class HealthCheckAdapter implements HealthCheckPort {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckAdapter.class);

    private final DataSource dataSource;
    private final MinioClient minioClient;

    public HealthCheckAdapter(DataSource dataSource, MinioClient minioClient) {
        this.dataSource = dataSource;
        this.minioClient = minioClient;
    }

    @Override
    public List<ServiceHealth> checkAll() {
        List<ServiceHealth> results = new ArrayList<>();
        results.add(checkDatabase());
        results.add(checkMinio());
        results.add(checkDisk());
        return results;
    }

    @Override
    public SystemHealthSnapshot.JvmMetrics getJvmMetrics() {
        var runtime = Runtime.getRuntime();
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMb = runtime.maxMemory() / (1024 * 1024);
        int memPercent = maxMb > 0 ? (int) ((usedMb * 100) / maxMb) : 0;
        int activeThreads = Thread.activeCount();

        return new SystemHealthSnapshot.JvmMetrics(
                formatUptime(uptimeMs),
                usedMb,
                maxMb,
                memPercent,
                activeThreads
        );
    }

    private ServiceHealth checkDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            long start = System.nanoTime();
            conn.createStatement().execute("SELECT 1");
            long pingMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return ServiceHealth.up("database", "Ping: " + pingMs + "ms");
        } catch (Exception e) {
            log.warn("Database health check failed", e);
            return ServiceHealth.down("database", e.getMessage());
        }
    }

    private ServiceHealth checkMinio() {
        try {
            long start = System.nanoTime();
            minioClient.listBuckets(ListBucketsArgs.builder().build());
            long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            return ServiceHealth.up("minio", "Latency: " + latencyMs + "ms");
        } catch (Exception e) {
            log.warn("MinIO health check failed", e);
            return ServiceHealth.down("minio", e.getMessage());
        }
    }

    private ServiceHealth checkDisk() {
        try {
            File root = new File("/");
            long freeGb = root.getFreeSpace() / (1024 * 1024 * 1024);
            long totalGb = root.getTotalSpace() / (1024 * 1024 * 1024);
            long usedGb = totalGb - freeGb;
            int usedPercent = totalGb > 0 ? (int) ((usedGb * 100) / totalGb) : 0;
            String detail = freeGb + " GB free / " + totalGb + " GB total (" + usedPercent + "% used)";
            return freeGb > 1 ? ServiceHealth.up("disk", detail) : ServiceHealth.down("disk", detail);
        } catch (Exception e) {
            log.warn("Disk health check failed", e);
            return ServiceHealth.unknown("disk", e.getMessage());
        }
    }

    private String formatUptime(long uptimeMs) {
        long days = TimeUnit.MILLISECONDS.toDays(uptimeMs);
        long hours = TimeUnit.MILLISECONDS.toHours(uptimeMs) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMs) % 60;
        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
