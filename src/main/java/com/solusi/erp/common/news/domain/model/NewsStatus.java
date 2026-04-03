package com.solusi.erp.common.news.domain.model;

/**
 * Value Object representing the lifecycle of a News item.
 */
public enum NewsStatus {
    DRAFT,
    PENDING_APPROVAL,
    PUBLISHED,
    ARCHIVED,
    EXPIRED
}
