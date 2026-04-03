package com.solusi.erp.master.tax.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.math.BigDecimal;

/**
 * Aggregate Root: Tax.
 * 100% Pure Java Domain Model.
 */
public class Tax {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private BigDecimal rate;
    private String note;
    private Boolean isSubtract;
    private Boolean isActive;

    public Tax(AuditMetadata metadata, String code, String name, BigDecimal rate,
               String note, Boolean isSubtract, Boolean isActive) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.rate = rate;
        this.note = note;
        this.isSubtract = isSubtract;
        this.isActive = isActive;
    }

    public static Tax createNew(String code, String name, BigDecimal rate,
                                String note, Boolean isSubtract, Boolean isActive) {
        return new Tax(AuditMetadata.empty(), code, name, rate, note,
                isSubtract != null ? isSubtract : false,
                isActive != null ? isActive : true);
    }

    public void update(String name, BigDecimal rate, String note, Boolean isSubtract, Boolean isActive) {
        this.name = name;
        this.rate = rate;
        this.note = note;
        this.isSubtract = isSubtract != null ? isSubtract : false;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public BigDecimal getRate() { return rate; }
    public String getNote() { return note; }
    public Boolean getIsSubtract() { return isSubtract; }
    public Boolean getIsActive() { return isActive; }
}
