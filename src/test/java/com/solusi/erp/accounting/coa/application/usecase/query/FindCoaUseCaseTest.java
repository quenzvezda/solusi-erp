package com.solusi.erp.accounting.coa.application.usecase.query;

import com.solusi.erp.accounting.coa.domain.model.ChartOfAccount;
import com.solusi.erp.accounting.coa.domain.repository.CoaRepository;
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
@DisplayName("FindCoaUseCase Tests")
class FindCoaUseCaseTest {

    @Mock private CoaRepository repository;
    private FindCoaUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindCoaUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates to repository with keyword and pageable")
    void execute_delegatesToRepository() {
        Pageable pageable = new Pageable(0, 20);
        Page<ChartOfAccount> expected = new Page<>(List.of(), 0, 20, 0L);
        when(repository.findAll("cash", pageable)).thenReturn(expected);

        Page<ChartOfAccount> result = useCase.execute("cash", pageable);

        assertThat(result).isSameAs(expected);
        verify(repository).findAll("cash", pageable);
    }
}
