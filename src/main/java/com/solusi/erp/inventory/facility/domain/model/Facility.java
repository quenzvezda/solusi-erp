package com.solusi.erp.inventory.facility.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

public class Facility {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private Long ownerId;
    private String ownerName;
    private String addressLine1;
    private Long cityId;
    private String cityName;
    private String postalCode;
    private String note;
    private Boolean isActive;

    public Facility(AuditMetadata metadata, String code, String name, Long ownerId, String ownerName,
                    String addressLine1, Long cityId, String cityName, String postalCode, String note, Boolean isActive) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.addressLine1 = addressLine1;
        this.cityId = cityId;
        this.cityName = cityName;
        this.postalCode = postalCode;
        this.note = note;
        this.isActive = isActive;
    }

    public static Facility createNew(String name, Long ownerId, String addressLine1, Long cityId, String postalCode, String note, Boolean isActive) {
        return new Facility(AuditMetadata.empty(), null, name, ownerId, null, addressLine1, cityId, null, postalCode, note, isActive);
    }

    public void update(String name, Long ownerId, String addressLine1, Long cityId, String postalCode, String note, Boolean isActive) {
        this.name = name;
        this.ownerId = ownerId;
        this.addressLine1 = addressLine1;
        this.cityId = cityId;
        this.postalCode = postalCode;
        this.note = note;
        this.isActive = isActive;
    }

    public void assignCode(String code) { this.code = code; }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getAddressLine1() { return addressLine1; }
    public Long getCityId() { return cityId; }
    public String getCityName() { return cityName; }
    public String getPostalCode() { return postalCode; }
    public String getNote() { return note; }
    public Boolean getIsActive() { return isActive; }
}
