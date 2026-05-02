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

    @Column(name = "debit_account_id", nullable = false)
    private Long debitAccountId;

    @Column(name = "credit_account_id", nullable = false)
    private Long creditAccountId;

    @Column(name = "tax_account_id")
    private Long taxAccountId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public AccountingSchema() {
        this.isActive = true;
    }
}
