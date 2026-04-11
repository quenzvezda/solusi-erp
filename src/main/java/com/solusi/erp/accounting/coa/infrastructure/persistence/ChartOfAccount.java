package com.solusi.erp.accounting.coa.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "acc_chart_of_accounts")
@Getter
@Setter
public class ChartOfAccount extends BaseModel {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "normal_balance", nullable = false, length = 10)
    private String normalBalance;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false)
    private Integer level;

    @Column(name = "is_header", nullable = false)
    private Boolean isHeader;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public ChartOfAccount() {
        this.level = 1;
        this.isHeader = false;
        this.isActive = true;
    }
}
