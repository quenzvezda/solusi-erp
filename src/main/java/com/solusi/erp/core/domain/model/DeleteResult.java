package com.solusi.erp.core.domain.model;

/**
 * Result of a Smart Delete operation.
 * <p>
 * When a delete is requested, the system checks if the entity is in use:
 * <ul>
 *   <li>{@link #HARD_DELETED} — entity was not in use, permanently removed from database</li>
 *   <li>{@link #SOFT_DELETED} — entity was in use, deactivated (isActive=false) instead</li>
 * </ul>
 * <p>
 * Pure Java — no framework dependencies.
 *
 * @see <a href="docs/architecture/smart-delete-pattern.md">Smart Delete Pattern Guide</a>
 */
public enum DeleteResult {
    HARD_DELETED,
    SOFT_DELETED
}
