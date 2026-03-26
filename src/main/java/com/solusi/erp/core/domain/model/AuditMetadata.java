package com.solusi.erp.core.domain.model;

import java.time.LocalDateTime;

/**
 * Value Object to hold technical and audit metadata.
 * Keeps Domain Entities pure from technical "noise" while satisfying persistence needs.
 */
public record AuditMetadata(
    Long id,
    Long version,
    LocalDateTime createdDate,
    Long createdBy,
    LocalDateTime updatedDate,
    Long updatedBy
) {
    /**
     * Create metadata for a new entity.
     */
    public static AuditMetadata empty() {
        return new AuditMetadata(null, null, null, null, null, null);
    }
}
