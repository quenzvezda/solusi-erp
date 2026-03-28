package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.Tax;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteTaxUseCase Tests")
class DeleteTaxUseCaseTest {

    @Mock
    private TaxRepository repository;

    private DeleteTaxUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteTaxUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute performs soft delete by setting isActive to false")
    void execute_performsSoftDelete() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Tax existing = new Tax(metadata, "TX-01", "PPN", BigDecimal.ONE, null, false, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Tax.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute(1L);

        assertThat(existing.getIsActive()).isFalse();
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("execute throws DomainException when tax is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}
