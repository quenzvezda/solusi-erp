package com.solusi.erp.inventory.uom.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.model.UomType;

/**
 * Aggregate Root: UnitOfMeasure.
 * 100% Pure Java Domain Model.
 */
public class UnitOfMeasure {
    private final AuditMetadata metadata;
    private final String code;
    private String name;
    private UomType type;

    public UnitOfMeasure(AuditMetadata metadata, String code, String name, UomType type) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.type = type;
    }

    public static UnitOfMeasure createNew(String code, String name, UomType type) {
        return new UnitOfMeasure(AuditMetadata.empty(), code, name, type);
    }

    public void update(String name, UomType type) {
        this.name = name;
        this.type = type;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public UomType getType() { return type; }
}
