package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.model.PeriodStatus;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReopenPeriodUseCase Tests")
class ReopenPeriodUseCaseTest {

    @Mock private FiscalYearRepository repository;
    private ReopenPeriodUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReopenPeriodUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute reopens a CLOSED period")
    void execute_reopensClosedPeriod() {
        AuditMetadata fyMeta = new AuditMetadata(1L, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(10L, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.CLOSED);
        FiscalYear fy = new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
        when(repository.findByPeriodId(10L)).thenReturn(Optional.of(fy));
        when(repository.savePeriod(any(AccountingPeriod.class))).thenAnswer(i -> i.getArgument(0));

        AccountingPeriod result = useCase.execute(10L);

        assertThat(result.getStatus()).isEqualTo(PeriodStatus.OPEN);
        verify(repository).savePeriod(any(AccountingPeriod.class));
    }

    @Test
    @DisplayName("execute reopens a NEVER_OPENED period")
    void execute_reopensNeverOpenedPeriod() {
        AuditMetadata fyMeta = new AuditMetadata(1L, 1L, null, null, null, null);
        AuditMetadata pMeta = new AuditMetadata(10L, 1L, null, null, null, null);
        AccountingPeriod period = new AccountingPeriod(pMeta, "P01", "Jan", 1, 1L,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), PeriodStatus.NEVER_OPENED);
        FiscalYear fy = new FiscalYear(fyMeta, "FY-0001", "FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true,
                new ArrayList<>(List.of(period)));
        when(repository.findByPeriodId(10L)).thenReturn(Optional.of(fy));
        when(repository.savePeriod(any(AccountingPeriod.class))).thenAnswer(i -> i.getArgument(0));

        AccountingPeriod result = useCase.execute(10L);

        assertThat(result.getStatus()).isEqualTo(PeriodStatus.OPEN);
    }

    @Test
    @DisplayName("execute throws when period not found")
    void execute_throwsWhenNotFound() {
        when(repository.findByPeriodId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(999L))
                .isInstanceOf(DomainException.class);
    }
}
