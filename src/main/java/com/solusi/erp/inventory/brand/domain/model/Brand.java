package com.solusi.erp.inventory.brand.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: Brand.
 * 100% Pure Java Domain Model.
 */
public class Brand {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private String note;

    public Brand(AuditMetadata metadata, String code, String name, String note) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.note = note;
    }

    public static Brand createNew(String code, String name, String note) {
        return new Brand(AuditMetadata.empty(), code, name, note);
    }

    public void update(String name, String note) {
        this.name = name;
        this.note = note;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getNote() { return note; }
}
