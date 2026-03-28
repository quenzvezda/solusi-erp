package com.solusi.erp.master.tax.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindTaxesUseCase Tests")
class FindTaxesUseCaseTest {

    @Mock
    private TaxRepository repository;

    private FindTaxesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindTaxesUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword and pageable to repository and returns its result")
    void execute_delegatesKeywordAndPageableToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        Tax tax = Tax.createNew("TX-01", "PPN", BigDecimal.valueOf(11), null, false, true);
        Page<Tax> expectedPage = new Page<>(List.of(tax), 0, 20, 1L);

        when(repository.findAll("ppn", pageable)).thenReturn(expectedPage);

        Page<Tax> result = useCase.execute("ppn", pageable);

        assertThat(result).isEqualTo(expectedPage);
    }
}
