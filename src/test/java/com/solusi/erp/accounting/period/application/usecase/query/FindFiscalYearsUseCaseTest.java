package com.solusi.erp.accounting.period.application.usecase.query;

import com.solusi.erp.accounting.period.domain.model.FiscalYear;
import com.solusi.erp.accounting.period.domain.repository.FiscalYearRepository;
import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindFiscalYearsUseCase Tests")
class FindFiscalYearsUseCaseTest {

    @Mock private FiscalYearRepository repository;
    private FindFiscalYearsUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindFiscalYearsUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates to repository")
    void execute_delegatesToRepository() {
        Pageable pageable = new Pageable(0, 20);
        Page<FiscalYear> expected = new Page<>(List.of(), 0, 20, 0L);
        when(repository.findAll("2026", pageable)).thenReturn(expected);

        Page<FiscalYear> result = useCase.execute("2026", pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll("2026", pageable);
    }
}
