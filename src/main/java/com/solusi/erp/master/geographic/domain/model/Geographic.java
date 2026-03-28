package com.solusi.erp.master.geographic.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.model.GeographicType;

/**
 * Aggregate Root: Geographic.
 * 100% Pure Java Domain Model.
 */
public class Geographic {

    private final AuditMetadata metadata;
    private String code;
    private String name;
    private GeographicType type;
    private Long parentId;
    private String parentName;
    private Boolean isActive;

    public Geographic(AuditMetadata metadata, String code, String name,
                      GeographicType type, Long parentId, String parentName, Boolean isActive) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.type = type;
        this.parentId = parentId;
        this.parentName = parentName;
        this.isActive = isActive;
    }

    public static Geographic createNew(String code, String name, GeographicType type,
                                       Long parentId, String parentName, Boolean isActive) {
        return new Geographic(AuditMetadata.empty(), code, name, type, parentId, parentName, isActive);
    }

    public void update(String name, GeographicType type, Long parentId,
                       String parentName, Boolean isActive) {
        this.name = name;
        this.type = type;
        this.parentId = parentId;
        this.parentName = parentName;
        this.isActive = isActive;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId()           { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode()       { return code; }
    public String getName()       { return name; }
    public GeographicType getType() { return type; }
    public Long getParentId()     { return parentId; }
    public String getParentName() { return parentName; }
    public Boolean getIsActive()  { return isActive; }
}
