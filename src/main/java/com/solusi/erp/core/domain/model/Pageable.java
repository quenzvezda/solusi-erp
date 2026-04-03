package com.solusi.erp.core.domain.model;

/**
 * Pure Java pageable information used across application/domain layers.
 */
public final class Pageable {
    private final int page;
    private final int size;
    private final String sortField;
    private final String sortDir;

    public Pageable(int page, int size) {
        this(page, size, null, null);
    }

    public Pageable(int page, int size, String sortField, String sortDir) {
        this.page = page;
        this.size = size;
        this.sortField = (sortField != null && !sortField.isBlank()) ? sortField : null;
        this.sortDir = normalizeSortDir(sortDir);
    }

    public static Pageable of(int page, int size) {
        return new Pageable(page, size);
    }

    public static Pageable of(int page, int size, String sortField, String sortDir) {
        return new Pageable(page, size, sortField, sortDir);
    }

    public int page() {
        return page;
    }

    public int size() {
        return size;
    }

    public String sortField() {
        return sortField;
    }

    public String sortDir() {
        return sortDir;
    }

    public boolean isSorted() {
        return sortField != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pageable other)) return false;
        return page == other.page
                && size == other.size
                && java.util.Objects.equals(sortField, other.sortField)
                && java.util.Objects.equals(sortDir, other.sortDir);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(page, size, sortField, sortDir);
    }

    @Override
    public String toString() {
        return "Pageable{page=%d,size=%d,sortField=%s,sortDir=%s}"
                .formatted(page, size, sortField, sortDir);
    }

    private String normalizeSortDir(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return "asc";
        }
        return "desc".equalsIgnoreCase(sortDir) ? "desc" : "asc";
    }
}
