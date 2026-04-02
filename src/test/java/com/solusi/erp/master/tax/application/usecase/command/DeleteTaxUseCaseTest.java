package com.solusi.erp.master.tax.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.domain.model.DeleteResult;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.Tax;
import com.solusi.erp.master.tax.domain.port.TaxInUseChecker;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteTaxUseCase Tests")
class DeleteTaxUseCaseTest {

    @Mock
    private TaxRepository repository;

    @Mock
    private TaxInUseChecker inUseChecker;

    private DeleteTaxUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteTaxUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute hard-deletes when tax is not in use")
    void execute_hardDeletesWhenNotInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Tax existing = new Tax(metadata, "TX-01", "PPN", BigDecimal.ONE, null, false, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(false);

        DeleteResult result = useCase.execute(1L);

        verify(repository).delete(1L);
        assertEquals(DeleteResult.HARD_DELETED, result);
    }

    @Test
    @DisplayName("execute soft-deletes when tax is in use")
    void execute_softDeletesWhenInUse() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Tax existing = new Tax(metadata, "TX-01", "PPN", BigDecimal.ONE, null, false, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isInUse(1L)).thenReturn(true);
        when(repository.save(any(Tax.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeleteResult result = useCase.execute(1L);

        assertThat(existing.getIsActive()).isFalse();
        verify(repository).save(existing);
        assertEquals(DeleteResult.SOFT_DELETED, result);
    }

    @Test
    @DisplayName("execute throws DomainException when tax is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}
