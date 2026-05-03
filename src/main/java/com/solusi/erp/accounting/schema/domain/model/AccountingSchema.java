package com.solusi.erp.accounting.schema.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.util.List;

public class AccountingSchema {
    private final AuditMetadata metadata;
    private SchemaEventType eventType;
    private String description;
    private Boolean isActive;
    private List<AccountingSchemaLine> lines;

    public AccountingSchema(AuditMetadata metadata, SchemaEventType eventType, String description, Boolean isActive, List<AccountingSchemaLine> lines) {
        this.metadata = metadata;
        this.eventType = eventType;
        this.description = description;
        this.isActive = isActive;
        this.lines = validateLines(eventType, lines);
    }

    public static AccountingSchema createNew(SchemaEventType eventType, String description, Boolean isActive, List<AccountingSchemaLine> lines) {
        return new AccountingSchema(AuditMetadata.empty(), eventType, description, isActive != null ? isActive : true, lines);
    }

    public void update(String description, Boolean isActive, List<AccountingSchemaLine> lines) {
        this.description = description;
        this.isActive = isActive != null ? isActive : true;
        this.lines = validateLines(this.eventType, lines);
    }
    
    private List<AccountingSchemaLine> validateLines(SchemaEventType type, List<AccountingSchemaLine> newLines) {
        if (newLines == null || newLines.isEmpty()) {
            throw new DomainException("msg.error.schema.lines.empty");
        }
        for (AccountingSchemaLine line : newLines) {
            if (line.variable().getSupportedEvent() != type) {
                throw new DomainException("msg.error.schema.variable.unsupported");
            }
        }
        return List.copyOf(newLines);
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public SchemaEventType getEventType() { return eventType; }
    public String getDescription() { return description; }
    public Boolean getIsActive() { return isActive; }
    public List<AccountingSchemaLine> getLines() { return lines; }
}