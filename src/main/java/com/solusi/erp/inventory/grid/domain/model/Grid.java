package com.solusi.erp.inventory.grid.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

public class Grid {
    private final AuditMetadata metadata;
    private Long facilityId;
    private String facilityName;
    private String code;
    private String name;
    private String note;
    private Boolean isActive;

    public Grid(AuditMetadata metadata, Long facilityId, String facilityName,
                String code, String name, String note, Boolean isActive) {
        this.metadata = metadata;
        this.facilityId = facilityId;
        this.facilityName = facilityName;
        this.code = code;
        this.name = name;
        this.note = note;
        this.isActive = isActive;
    }

    public static Grid createNew(Long facilityId, String code, String name, String note, Boolean isActive) {
        return new Grid(AuditMetadata.empty(), facilityId, null, code, name, note, isActive);
    }

    public void update(String name, String note, Boolean isActive) {
        this.name = name;
        this.note = note;
        this.isActive = isActive;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getFacilityId() { return facilityId; }
    public String getFacilityName() { return facilityName; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getNote() { return note; }
    public Boolean getIsActive() { return isActive; }
}
