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

    private AccountingSchema(AuditMetadata metadata,
                             SchemaEventType eventType,
                             String description,
                             Boolean isActive,
                             List<AccountingSchemaLine> lines,
                             boolean validateLines) {
        this.metadata = metadata;
        this.eventType = eventType;
        this.description = description;
        this.isActive = isActive;
        this.lines = validateLines ? validateLines(eventType, lines) : copyLines(lines);
    }

    public AccountingSchema(AuditMetadata metadata,
                            SchemaEventType eventType,
                            String description,
                            Boolean isActive,
                            List<AccountingSchemaLine> lines) {
        this(metadata, eventType, description, isActive, lines, true);
    }

    public static AccountingSchema createNew(SchemaEventType eventType,
                                             String description,
                                             Boolean isActive,
                                             List<AccountingSchemaLine> lines) {
        return new AccountingSchema(AuditMetadata.empty(), eventType, description, isActive != null ? isActive : true, lines, true);
    }

    public static AccountingSchema reconstitute(AuditMetadata metadata,
                                                SchemaEventType eventType,
                                                String description,
                                                Boolean isActive,
                                                List<AccountingSchemaLine> lines) {
        return new AccountingSchema(metadata, eventType, description, isActive, lines, false);
    }

    public void update(String description, Boolean isActive, List<AccountingSchemaLine> lines) {
        this.description = description;
        this.isActive = isActive != null ? isActive : true;
        this.lines = validateLines(this.eventType, lines);
    }

    private List<AccountingSchemaLine> validateLines(SchemaEventType type, List<AccountingSchemaLine> newLines) {
        if (newLines == null || newLines.isEmpty()) {
            throw new DomainException("msg.err.schema.lines.empty");
        }
        for (AccountingSchemaLine line : newLines) {
            if (line.getVar().getSupportedEvent() != type) {
                throw new DomainException("msg.err.schema.var.unsupported");
            }
        }
        return List.copyOf(newLines);
    }

    private List<AccountingSchemaLine> copyLines(List<AccountingSchemaLine> sourceLines) {
        if (sourceLines == null || sourceLines.isEmpty()) {
            return List.of();
        }
        return List.copyOf(sourceLines);
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
