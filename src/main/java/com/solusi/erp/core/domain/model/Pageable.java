package com.solusi.erp.core.domain.model;

import java.util.List;

/**
 * Pure Java Pageable information.
 */
public record Pageable(int page, int size) {
    public static Pageable of(int page, int size) {
        return new Pageable(page, size);
    }
}
