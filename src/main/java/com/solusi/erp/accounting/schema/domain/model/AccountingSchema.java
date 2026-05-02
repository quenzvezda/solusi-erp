package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

/**
 * Aggregate Root: Accounting Schema.
 * Maps an operational event to a debit/credit COA pair for auto-journaling.
 * 100% Pure Java Domain Model.
 */
public class AccountingSchema {
    private final AuditMetadata metadata;
    private SchemaEventType eventType;
    private String description;
    private Long debitAccountId;
    private Long creditAccountId;
    private Long taxAccountId;
    private Boolean isActive;

    public AccountingSchema(AuditMetadata metadata, SchemaEventType eventType,
                            String description, Long debitAccountId,
                            Long creditAccountId, Long taxAccountId, Boolean isActive) {
        this.metadata = metadata;
        this.eventType = eventType;
        this.description = description;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.taxAccountId = taxAccountId;
        this.isActive = isActive;
    }

    public static AccountingSchema createNew(SchemaEventType eventType, String description,
                                             Long debitAccountId, Long creditAccountId,
                                             Boolean isActive) {
        return createNew(eventType, description, debitAccountId, creditAccountId, null, isActive);
    }

    public static AccountingSchema createNew(SchemaEventType eventType, String description,
                                             Long debitAccountId, Long creditAccountId,
                                             Long taxAccountId, Boolean isActive) {
        return new AccountingSchema(AuditMetadata.empty(), eventType, description,
                debitAccountId, creditAccountId, taxAccountId,
                isActive != null ? isActive : true);
    }

    public void update(String description, Long debitAccountId,
                       Long creditAccountId, Boolean isActive) {
        update(description, debitAccountId, creditAccountId, null, isActive);
    }

    public void update(String description, Long debitAccountId,
                       Long creditAccountId, Long taxAccountId, Boolean isActive) {
        this.description = description;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.taxAccountId = taxAccountId;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public SchemaEventType getEventType() { return eventType; }
    public String getDescription() { return description; }
    public Long getDebitAccountId() { return debitAccountId; }
    public Long getCreditAccountId() { return creditAccountId; }
    public Long getTaxAccountId() { return taxAccountId; }
    public Boolean getIsActive() { return isActive; }
}
