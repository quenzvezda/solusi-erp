package com.solusi.erp.inventory.productcategory.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.productcategory.domain.model.ProductCategoryType;

public class ProductCategory {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private ProductCategoryType type;
    private String note;

    public ProductCategory(AuditMetadata metadata, String code, String name, ProductCategoryType type, String note) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.type = type;
        this.note = note;
    }

    public static ProductCategory createNew(String code, String name, ProductCategoryType type, String note) {
        return new ProductCategory(AuditMetadata.empty(), code, name, type, note);
    }

    public void update(String name, ProductCategoryType type, String note) {
        this.name = name;
        this.type = type;
        this.note = note;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public ProductCategoryType getType() { return type; }
    public String getNote() { return note; }
}
