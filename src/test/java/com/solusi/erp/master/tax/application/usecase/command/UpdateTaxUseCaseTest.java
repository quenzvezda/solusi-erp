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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateTaxUseCase Tests")
class UpdateTaxUseCaseTest {

    @Mock
    private TaxRepository repository;

    private UpdateTaxUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateTaxUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates mutable fields of an existing tax")
    void execute_updatesMutableFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Tax existing = new Tax(metadata, "TX-01", "Old Name", BigDecimal.ONE, "Old Note", false, true, TaxCalculationMode.EXCLUSIVE);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Tax.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Tax result = useCase.execute(1L, "New Name", BigDecimal.TEN, "New Note", true, false, TaxCalculationMode.INCLUSIVE);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getRate()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(result.getCode()).isEqualTo("TX-01");
        assertThat(result.getCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
    }

    @Test
    @DisplayName("execute throws DomainException when tax is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class,
                () -> useCase.execute(99L, "Name", BigDecimal.ONE, null, false, true, TaxCalculationMode.EXCLUSIVE));
    }
}
