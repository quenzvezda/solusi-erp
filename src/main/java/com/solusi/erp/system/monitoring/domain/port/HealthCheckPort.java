package com.solusi.erp.system.monitoring.domain.port;

import com.solusi.erp.system.monitoring.domain.model.ServiceHealth;
import com.solusi.erp.system.monitoring.domain.model.SystemHealthSnapshot;

import java.util.List;

/**
 * Port for checking health of infrastructure services.
 */
public interface HealthCheckPort {
    List<ServiceHealth> checkAll();
    SystemHealthSnapshot.JvmMetrics getJvmMetrics();
}
