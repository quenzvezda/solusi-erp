package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.PeriodStatus;
import com.solusi.erp.accounting.period.domain.port.OpenAccountingPeriodLookup;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnsureOpenPeriodForDateUseCaseTest {

    @Mock
    private OpenAccountingPeriodLookup openAccountingPeriodLookup;

    private EnsureOpenPeriodForDateUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new EnsureOpenPeriodForDateUseCaseImpl(openAccountingPeriodLookup);
    }

    @Test
    void execute_whenNoOpenPeriodForDate_throws() {
        LocalDate receiptDate = LocalDate.of(2026, 4, 26);
        when(openAccountingPeriodLookup.findOpenPeriodContaining(receiptDate))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(receiptDate))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.period.not.open");
    }

    @Test
    void execute_whenOpenPeriodExists_doesNotThrow() {
        LocalDate receiptDate = LocalDate.of(2026, 4, 26);
        when(openAccountingPeriodLookup.findOpenPeriodContaining(receiptDate))
                .thenReturn(Optional.of(new AccountingPeriod(
                        new AuditMetadata(1L, 1L, null, null, null, null),
                        "FY-2026-04",
                        "Apr 2026",
                        4,
                        1L,
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 30),
                        PeriodStatus.OPEN
                )));

        assertThatCode(() -> useCase.execute(receiptDate)).doesNotThrowAnyException();
        verify(openAccountingPeriodLookup).findOpenPeriodContaining(receiptDate);
    }
}
