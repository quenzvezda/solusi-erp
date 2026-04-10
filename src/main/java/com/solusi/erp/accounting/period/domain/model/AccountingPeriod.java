package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.time.LocalDate;

/**
 * Entity within the FiscalYear aggregate.
 * Represents a single accounting period (typically one month).
 * 100% Pure Java.
 */
public class AccountingPeriod {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private int periodNumber;
    private Long fiscalYearId;
    private LocalDate startDate;
    private LocalDate endDate;
    private PeriodStatus status;

    public AccountingPeriod(AuditMetadata metadata, String code, String name,
                            int periodNumber, Long fiscalYearId,
                            LocalDate startDate, LocalDate endDate,
                            PeriodStatus status) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.periodNumber = periodNumber;
        this.fiscalYearId = fiscalYearId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public static AccountingPeriod createNew(String code, String name, int periodNumber,
                                              Long fiscalYearId,
                                              LocalDate startDate, LocalDate endDate) {
        return new AccountingPeriod(AuditMetadata.empty(), code, name,
                periodNumber, fiscalYearId, startDate, endDate, PeriodStatus.NEVER_OPENED);
    }

    public void open() {
        this.status = PeriodStatus.OPEN;
    }

    public void close() {
        this.status = PeriodStatus.CLOSED;
    }

    public void reopen() {
        this.status = PeriodStatus.OPEN;
    }

    public boolean isOpen() {
        return this.status == PeriodStatus.OPEN;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public int getPeriodNumber() { return periodNumber; }
    public Long getFiscalYearId() { return fiscalYearId; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public PeriodStatus getStatus() { return status; }
}
