package com.solusi.erp.accounting.period.application.usecase.command;

import com.solusi.erp.accounting.period.domain.model.AccountingPeriod;
import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateFiscalYearUseCase Tests")
class CreateFiscalYearUseCaseTest {

    @Mock private FiscalYearRepository repository;
    @Mock private SequenceGeneratorService sequenceGenerator;
    private CreateFiscalYearUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateFiscalYearUseCaseImpl(repository, sequenceGenerator);
    }

    @Test
    @DisplayName("execute generates code and saves fiscal year with periods")
    void execute_generatesCodeAndSavesFiscalYear() {
        when(sequenceGenerator.generate("FISCAL_YEAR")).thenReturn("FY-0001");
        when(repository.existsByCode("FY-0001")).thenReturn(false);
        AuditMetadata savedMeta = new AuditMetadata(1L, 1L, null, null, null, null);
        when(repository.save(any(FiscalYear.class))).thenAnswer(i -> {
            FiscalYear input = i.getArgument(0);
            return new FiscalYear(savedMeta, input.getCode(), input.getName(),
                    input.getStartDate(), input.getEndDate(), input.getIsActive(), input.getPeriods());
        });
        when(repository.savePeriods(anyList())).thenAnswer(i -> i.getArgument(0));

        FiscalYear result = useCase.execute("FY 2026",
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31), true);

        assertThat(result.getCode()).isEqualTo("FY-0001");
        assertThat(result.getName()).isEqualTo("FY 2026");
        assertThat(result.getPeriods()).hasSize(12);
        verify(sequenceGenerator).generate("FISCAL_YEAR");
        verify(repository).save(any(FiscalYear.class));
        verify(repository).savePeriods(anyList());
    }
}
