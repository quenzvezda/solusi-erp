package com.solusi.erp.accounting.period.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AccountingPeriod Domain Model Tests")
class AccountingPeriodTest {

    @Test
    @DisplayName("createNew defaults to NEVER_OPENED status")
    void createNew_defaultsToNeverOpened() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan 2026", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        assertThat(period.getStatus()).isEqualTo(PeriodStatus.NEVER_OPENED);
        assertThat(period.isOpen()).isFalse();
    }

    @Test
    @DisplayName("open sets status to OPEN")
    void open_setsStatusToOpen() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        period.open();
        assertThat(period.getStatus()).isEqualTo(PeriodStatus.OPEN);
        assertThat(period.isOpen()).isTrue();
    }

    @Test
    @DisplayName("close sets status to CLOSED")
    void close_setsStatusToClosed() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        period.open();
        period.close();
        assertThat(period.getStatus()).isEqualTo(PeriodStatus.CLOSED);
        assertThat(period.isOpen()).isFalse();
    }

    @Test
    @DisplayName("reopen sets status back to OPEN")
    void reopen_setsStatusToOpen() {
        AccountingPeriod period = AccountingPeriod.createNew(
                "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        period.open();
        period.close();
        period.reopen();
        assertThat(period.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    @DisplayName("all getters return correct values")
    void getters_returnCorrectValues() {
        AuditMetadata meta = new AuditMetadata(5L, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(meta, "FY-0001-03", "Mar 2026", 3, 1L,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), PeriodStatus.OPEN);
        assertThat(period.getId()).isEqualTo(5L);
        assertThat(period.getCode()).isEqualTo("FY-0001-03");
        assertThat(period.getName()).isEqualTo("Mar 2026");
        assertThat(period.getPeriodNumber()).isEqualTo(3);
        assertThat(period.getFiscalYearId()).isEqualTo(1L);
    }
}
