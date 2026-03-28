package com.solusi.erp.master.partyroletype.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: PartyRoleType.
 * 100% Pure Java Domain Model.
 */
public class PartyRoleType {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private String note;
    private Boolean isActive;

    public PartyRoleType(AuditMetadata metadata, String code, String name, String note, Boolean isActive) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.note = note;
        this.isActive = isActive;
    }

    public static PartyRoleType createNew(String code, String name, String note, Boolean isActive) {
        return new PartyRoleType(AuditMetadata.empty(), code, name, note,
                isActive != null ? isActive : true);
    }

    public void update(String name, String note, Boolean isActive) {
        this.name = name;
        this.note = note;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getNote() { return note; }
    public Boolean getIsActive() { return isActive; }
}
