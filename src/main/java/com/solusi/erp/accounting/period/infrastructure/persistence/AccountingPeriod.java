package com.solusi.erp.accounting.period.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "acc_accounting_periods")
@Getter
@Setter
public class AccountingPeriod extends BaseModel {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "period_number", nullable = false)
    private Integer periodNumber;

    @Column(name = "fiscal_year_id", nullable = false)
    private Long fiscalYearId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_year_id", insertable = false, updatable = false)
    private FiscalYear fiscalYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 15)
    private String status;

    public AccountingPeriod() {
        this.status = "NEVER_OPENED";
    }
}
