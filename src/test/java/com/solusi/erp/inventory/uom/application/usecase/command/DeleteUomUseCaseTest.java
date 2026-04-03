package com.solusi.erp.inventory.uom.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.uom.domain.model.UnitOfMeasure;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import com.solusi.erp.inventory.uom.domain.port.UomUsageChecker;
import com.solusi.erp.inventory.uom.domain.repository.UomRepository;
import com.solusi.erp.inventory.uom.infrastructure.adapter.UomInUseCheckerComposite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DeleteUomUseCase Tests")
class DeleteUomUseCaseTest {

    private UomRepository repository;
    private UomUsageChecker productChecker;
    private UomUsageChecker adjustmentLineChecker;
    private DeleteUomUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(UomRepository.class);
        productChecker = mock(UomUsageChecker.class);
        adjustmentLineChecker = mock(UomUsageChecker.class);
        UomInUseCheckerComposite composite = new UomInUseCheckerComposite(
                List.of(productChecker, adjustmentLineChecker));
        useCase = new DeleteUomUseCaseImpl(repository, composite);
    }

    private UnitOfMeasure stubUom(Long id) {
        AuditMetadata meta = new AuditMetadata(id, id, null, null, null, null);
        return new UnitOfMeasure(meta, "PCS", "Pieces", UomType.UNIT);
    }

    @Test
    @DisplayName("execute deletes UoM when no checker reports it as in-use")
    void execute_deletesUom_whenNotInUse() {
        when(repository.findById(1L)).thenReturn(Optional.of(stubUom(1L)));
        when(productChecker.isUsed(1L)).thenReturn(false);
        when(adjustmentLineChecker.isUsed(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when UoM is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessage("msg.error.uom.notfound");
        verify(repository, never()).delete(99L);
    }

    @Test
    @DisplayName("execute throws DomainException when UoM is used by a Product")
    void execute_throwsDomainException_whenUsedByProduct() {
        when(repository.findById(2L)).thenReturn(Optional.of(stubUom(2L)));
        when(productChecker.isUsed(2L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(2L));
        verify(repository, never()).delete(2L);
    }

    @Test
    @DisplayName("execute throws DomainException when UoM is used by a StockAdjustmentLine")
    void execute_throwsDomainException_whenUsedByAdjustmentLine() {
        when(repository.findById(3L)).thenReturn(Optional.of(stubUom(3L)));
        when(productChecker.isUsed(3L)).thenReturn(false);
        when(adjustmentLineChecker.isUsed(3L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(3L));
        verify(repository, never()).delete(3L);
    }
}
