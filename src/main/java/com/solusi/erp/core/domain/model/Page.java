package com.solusi.erp.core.domain.model;

import java.util.List;

/**
 * Pure Java Page response.
 */
public record Page<T>(
    List<T> content,
    int page,
    int size,
    long totalElements
) {
    public int getTotalPages() {
        return size == 0 ? 1 : (int) Math.ceil((double) totalElements / (double) size);
    }
}
