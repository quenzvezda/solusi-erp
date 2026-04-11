package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Root: Fiscal Year.
 * Creates and manages AccountingPeriod children.
 * 100% Pure Java Domain Model.
 */
public class FiscalYear {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
    private List<AccountingPeriod> periods;

    public FiscalYear(AuditMetadata metadata, String code, String name,
                      LocalDate startDate, LocalDate endDate, Boolean isActive,
                      List<AccountingPeriod> periods) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.periods = periods != null ? periods : new ArrayList<>();
    }

    public static FiscalYear createNew(String code, String name,
                                        LocalDate startDate, LocalDate endDate,
                                        Boolean isActive) {
        return new FiscalYear(AuditMetadata.empty(), code, name,
                startDate, endDate,
                isActive != null ? isActive : true,
                new ArrayList<>());
    }

    /**
     * Auto-generates 12 monthly periods based on the fiscal year date range.
     * Each period spans one calendar month (or partial month for first/last).
     */
    public List<AccountingPeriod> generateMonthlyPeriods(Long fiscalYearId) {
        List<AccountingPeriod> generated = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");
        YearMonth startMonth = YearMonth.from(startDate);
        YearMonth endMonth = YearMonth.from(endDate);
        int seq = 1;

        for (YearMonth ym = startMonth; !ym.isAfter(endMonth); ym = ym.plusMonths(1)) {
            LocalDate periodStart = ym.equals(startMonth) ? startDate : ym.atDay(1);
            LocalDate periodEnd = ym.equals(endMonth) ? endDate : ym.atEndOfMonth();
            String periodCode = code + "-" + String.format("%02d", seq);
            String periodName = ym.format(monthFormatter);

            generated.add(AccountingPeriod.createNew(
                    periodCode, periodName, seq, fiscalYearId, periodStart, periodEnd));
            seq++;
        }
        this.periods = generated;
        return generated;
    }

    public List<AccountingPeriod> getOpenPeriods() {
        return periods.stream().filter(AccountingPeriod::isOpen).toList();
    }

    public AccountingPeriod closePeriod(Long periodId) {
        AccountingPeriod period = periods.stream()
                .filter(p -> p.getId().equals(periodId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Period not found in this fiscal year"));
        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("Only OPEN periods can be closed");
        }
        period.close();
        return period;
    }

    public AccountingPeriod reopenPeriod(Long periodId) {
        AccountingPeriod period = periods.stream()
                .filter(p -> p.getId().equals(periodId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Period not found in this fiscal year"));
        if (period.getStatus() == PeriodStatus.OPEN) {
            throw new IllegalStateException("Period is already open");
        }
        period.reopen();
        return period;
    }

    public void update(String name, Boolean isActive) {
        this.name = name;
        this.isActive = isActive != null ? isActive : true;
    }

    public void softDelete() {
        this.isActive = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public Boolean getIsActive() { return isActive; }
    public List<AccountingPeriod> getPeriods() { return periods; }
}
