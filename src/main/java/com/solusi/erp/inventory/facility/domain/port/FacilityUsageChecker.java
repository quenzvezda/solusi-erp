package com.solusi.erp.inventory.facility.domain.port;

/**
 * Cross-slice usage checker port for Facility.
 * Any slice that references a Facility must implement this interface
 * and register it as a Spring bean so FacilityInUseCheckerComposite
 * can aggregate all checks automatically.
 */
public interface FacilityUsageChecker {
    boolean isUsed(Long facilityId);
}
