package com.solusi.erp.inventory.container.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import java.math.BigDecimal;

public class Container {
    private final AuditMetadata metadata;
    private Long gridId;
    private String gridName;
    private String facilityName;
    private String code;
    private String name;
    private String barcode;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal maxWeight;
    private String note;
    private Boolean isActive;

    public Container(AuditMetadata metadata, Long gridId, String gridName, String facilityName,
                     String code, String name, String barcode, BigDecimal length, BigDecimal width,
                     BigDecimal height, BigDecimal maxWeight, String note, Boolean isActive) {
        this.metadata = metadata;
        this.gridId = gridId;
        this.gridName = gridName;
        this.facilityName = facilityName;
        this.code = code;
        this.name = name;
        this.barcode = barcode;
        this.length = length;
        this.width = width;
        this.height = height;
        this.maxWeight = maxWeight;
        this.note = note;
        this.isActive = isActive;
    }

    public static Container createNew(Long gridId, String name, String barcode,
                                       BigDecimal length, BigDecimal width, BigDecimal height,
                                       BigDecimal maxWeight, String note, Boolean isActive) {
        return new Container(AuditMetadata.empty(), gridId, null, null, null,
            name, barcode, length, width, height, maxWeight, note, isActive);
    }

    public void assignCode(String code) { this.code = code; }

    public void update(String name, String barcode, BigDecimal length, BigDecimal width,
                       BigDecimal height, BigDecimal maxWeight, String note, Boolean isActive) {
        this.name = name;
        this.barcode = barcode;
        this.length = length;
        this.width = width;
        this.height = height;
        this.maxWeight = maxWeight;
        this.note = note;
        this.isActive = isActive;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getGridId() { return gridId; }
    public String getGridName() { return gridName; }
    public String getFacilityName() { return facilityName; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public BigDecimal getLength() { return length; }
    public BigDecimal getWidth() { return width; }
    public BigDecimal getHeight() { return height; }
    public BigDecimal getMaxWeight() { return maxWeight; }
    public String getNote() { return note; }
    public Boolean getIsActive() { return isActive; }
}
