package com.solusi.erp.master.currency.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: Currency.
 * 100% Pure Java Domain Model.
 */
public class Currency {
    private final AuditMetadata metadata;
    private String symbol;
    private String alias;
    private String name;
    private String note;
    private Boolean isDefault;
    private Boolean isActive;

    public Currency(AuditMetadata metadata, String symbol, String alias, String name,
                    String note, Boolean isDefault, Boolean isActive) {
        this.metadata = metadata;
        this.symbol = symbol;
        this.alias = alias;
        this.name = name;
        this.note = note;
        this.isDefault = isDefault;
        this.isActive = isActive;
    }

    public static Currency createNew(String symbol, String alias, String name,
                                     String note, Boolean isDefault, Boolean isActive) {
        return new Currency(AuditMetadata.empty(), symbol, alias, name, note,
                isDefault != null ? isDefault : false,
                isActive != null ? isActive : true);
    }

    public void update(String symbol, String name, String note, Boolean isDefault, Boolean isActive) {
        this.symbol = symbol;
        this.name = name;
        this.note = note;
        this.isDefault = isDefault != null ? isDefault : false;
        this.isActive = isActive != null ? isActive : true;
    }

    public void unsetDefault() {
        this.isDefault = false;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getSymbol() { return symbol; }
    public String getAlias() { return alias; }
    public String getName() { return name; }
    public String getNote() { return note; }
    public Boolean getIsDefault() { return isDefault; }
    public Boolean getIsActive() { return isActive; }
}
