package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FiscalYear Domain Model Tests")
class FiscalYearTest {

    @Test
    @DisplayName("createNew sets fields with empty periods")
    void createNew_setsFieldsWithEmptyPeriods() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);
        assertThat(fy.getId()).isNull();
        assertThat(fy.getCode()).isEqualTo("FY-0001");
        assertThat(fy.getName()).isEqualTo("FY 2026");
        assertThat(fy.getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(fy.getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(fy.getIsActive()).isTrue();
        assertThat(fy.getPeriods()).isEmpty();
    }

    @Test
    @DisplayName("generateMonthlyPeriods creates 12 periods for full calendar year")
    void generateMonthlyPeriods_creates12PeriodsForFullYear() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);
        List<AccountingPeriod> periods = fy.generateMonthlyPeriods(1L);
        assertThat(periods).hasSize(12);
        assertThat(periods.get(0).getCode()).isEqualTo("FY-0001-01");
        assertThat(periods.get(0).getPeriodNumber()).isEqualTo(1);
        assertThat(periods.get(0).getStartDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(periods.get(0).getEndDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(periods.get(0).getStatus()).isEqualTo(PeriodStatus.NEVER_OPENED);
        assertThat(periods.get(11).getCode()).isEqualTo("FY-0001-12");
        assertThat(periods.get(11).getEndDate()).isEqualTo(LocalDate.of(2026, 12, 31));
    }

    @Test
    @DisplayName("closePeriod transitions OPEN period to CLOSED")
    void closePeriod_transitionsOpenToClosed() {
        FiscalYear fy = buildFyWithOpenPeriod(1L, 10L);
        AccountingPeriod closed = fy.closePeriod(10L);
        assertThat(closed.getStatus()).isEqualTo(PeriodStatus.CLOSED);
    }

    @Test
    @DisplayName("closePeriod throws when period is NEVER_OPENED")
    void closePeriod_throwsWhenNeverOpened() {
        FiscalYear fy = buildFyWithNeverOpenedPeriod(1L, 10L);
        assertThatThrownBy(() -> fy.closePeriod(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only OPEN periods can be closed");
    }

    @Test
    @DisplayName("closePeriod throws when period not found")
    void closePeriod_throwsWhenPeriodNotFound() {
        FiscalYear fy = buildFyWithOpenPeriod(1L, 10L);
        assertThatThrownBy(() -> fy.closePeriod(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Period not found");
    }

    @Test
    @DisplayName("reopenPeriod transitions CLOSED to OPEN")
    void reopenPeriod_transitionsClosedToOpen() {
        FiscalYear fy = buildFyWithClosedPeriod(1L, 10L);
        AccountingPeriod reopened = fy.reopenPeriod(10L);
        assertThat(reopened.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    @DisplayName("reopenPeriod transitions NEVER_OPENED to OPEN")
    void reopenPeriod_transitionsNeverOpenedToOpen() {
        FiscalYear fy = buildFyWithNeverOpenedPeriod(1L, 10L);
        AccountingPeriod reopened = fy.reopenPeriod(10L);
        assertThat(reopened.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    @DisplayName("reopenPeriod throws when period is already OPEN")
    void reopenPeriod_throwsWhenAlreadyOpen() {
        FiscalYear fy = buildFyWithOpenPeriod(1L, 10L);
        assertThatThrownBy(() -> fy.reopenPeriod(10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already open");
    }

    @Test
    @DisplayName("getOpenPeriods returns only OPEN status periods")
    void getOpenPeriods_returnsOnlyOpenPeriods() {
        AuditMetadata meta = new AuditMetadata(1L, 1L, null, null, null, null);
        AuditMetadata pMeta1 = new AuditMetadata(10L, 1L, null, null, null, null);
        AuditMetadata pMeta2 = new AuditMetadata(11L, 1L, null, null, null, null);
        AccountingPeriod open = new AccountingPeriod(pMeta1, "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.OPEN);
        AccountingPeriod closed = new AccountingPeriod(pMeta2, "P02", "Feb", 2, 1L,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28), PeriodStatus.CLOSED);
        FiscalYear fy = new FiscalYear(meta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(open, closed)));
        assertThat(fy.getOpenPeriods()).hasSize(1);
        assertThat(fy.getOpenPeriods().get(0).getCode()).isEqualTo("P01");
    }

    @Test
    @DisplayName("update changes name and isActive only")
    void update_changesNameAndIsActive() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "Old", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);
        fy.update("New Name", false);
        assertThat(fy.getName()).isEqualTo("New Name");
        assertThat(fy.getIsActive()).isFalse();
        assertThat(fy.getCode()).isEqualTo("FY-0001");
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveFalse() {
        FiscalYear fy = FiscalYear.createNew("FY-0001", "FY", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31), true);
        fy.softDelete();
        assertThat(fy.getIsActive()).isFalse();
    }

    // --- helpers ---
    private FiscalYear buildFyWithOpenPeriod(Long fyId, Long periodId) {
        AuditMetadata fyMeta = new AuditMetadata(fyId, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(periodId, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, fyId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.OPEN);
        return new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
    }

    private FiscalYear buildFyWithNeverOpenedPeriod(Long fyId, Long periodId) {
        AuditMetadata fyMeta = new AuditMetadata(fyId, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(periodId, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, fyId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.NEVER_OPENED);
        return new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
    }

    private FiscalYear buildFyWithClosedPeriod(Long fyId, Long periodId) {
        AuditMetadata fyMeta = new AuditMetadata(fyId, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(periodId, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, fyId,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.CLOSED);
        return new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
    }
}
