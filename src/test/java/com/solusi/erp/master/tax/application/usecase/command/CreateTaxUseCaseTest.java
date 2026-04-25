package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.master.tax.domain.repository.TaxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateTaxUseCase Tests")
class CreateTaxUseCaseTest {

    @Mock
    private TaxRepository repository;

    private CreateTaxUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateTaxUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute saves and returns tax when code is unique")
    void execute_savesTaxWhenCodeIsUnique() {
        when(repository.existsByCode("TX-01")).thenReturn(false);
        when(repository.save(any(Tax.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tax result = useCase.execute("TX-01", "PPN", BigDecimal.valueOf(11), "Note", false, true, TaxCalculationMode.INCLUSIVE);

        assertThat(result.getCode()).isEqualTo("TX-01");
        assertThat(result.getName()).isEqualTo("PPN");
        assertThat(result.getCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
    }

    @Test
    @DisplayName("execute throws DomainException when code already exists")
    void execute_throwsDomainException_whenCodeExists() {
        when(repository.existsByCode("TX-01")).thenReturn(true);

        assertThrows(DomainException.class,
                () -> useCase.execute("TX-01", "PPN", BigDecimal.valueOf(11), null, false, true, TaxCalculationMode.EXCLUSIVE));
    }

    @Test
    @DisplayName("execute returns result from repository with persisted id")
    void execute_returnsResultFromRepository() {
        AuditMetadata metadata = new AuditMetadata(10L, 1L, null, null, null, null);
        Tax persisted = new Tax(metadata, "TX-01", "PPN", BigDecimal.valueOf(11), null, false, true, TaxCalculationMode.EXCLUSIVE);

        when(repository.existsByCode("TX-01")).thenReturn(false);
        when(repository.save(any(Tax.class))).thenReturn(persisted);

        Tax result = useCase.execute("TX-01", "PPN", BigDecimal.valueOf(11), null, false, true, TaxCalculationMode.EXCLUSIVE);

        assertThat(result.getId()).isEqualTo(10L);
    }
}
