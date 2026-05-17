package com.solusi.erp.accounting.schema.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "acc_accounting_schemas")
@Getter
@Setter
public class AccountingSchema extends BaseModel {

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @jakarta.persistence.OneToMany(mappedBy = "schema", cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private java.util.List<AccountingSchemaLineEntity> lines = new java.util.ArrayList<>();

    public void setLines(java.util.List<AccountingSchemaLineEntity> newLines) {
        this.lines.clear();
        if (newLines != null) {
            newLines.forEach(line -> {
                line.setSchema(this);
                this.lines.add(line);
            });
        }
    }

    public AccountingSchema() {
        this.isActive = true;
    }
}
