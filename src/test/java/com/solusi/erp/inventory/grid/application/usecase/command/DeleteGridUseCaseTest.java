package com.solusi.erp.inventory.grid.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.grid.domain.model.Grid;
import com.solusi.erp.inventory.grid.domain.port.GridUsageChecker;
import com.solusi.erp.inventory.grid.domain.repository.GridRepository;
import com.solusi.erp.inventory.grid.infrastructure.adapter.GridInUseCheckerComposite;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DeleteGridUseCase Tests")
class DeleteGridUseCaseTest {

    private GridRepository repository;
    private GridUsageChecker checkerA;
    private GridUsageChecker checkerB;
    private DeleteGridUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(GridRepository.class);
        checkerA = mock(GridUsageChecker.class);
        checkerB = mock(GridUsageChecker.class);
        GridInUseCheckerComposite composite = new GridInUseCheckerComposite(List.of(checkerA, checkerB));
        useCase = new DeleteGridUseCaseImpl(repository, composite);
    }

    private Grid stubGrid(Long id) {
        AuditMetadata meta = new AuditMetadata(id, id, null, null, null, null);
        return new Grid(meta, 1L, null, "GRD-001", "Grid A", null, true);
    }

    @Test
    @DisplayName("execute deletes grid when no checker reports it as in-use")
    void execute_deletesGrid_whenNotInUse() {
        when(repository.findById(1L)).thenReturn(Optional.of(stubGrid(1L)));
        when(checkerA.isUsed(1L)).thenReturn(false);
        when(checkerB.isUsed(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when grid is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
        verify(repository, never()).delete(99L);
    }

    @Test
    @DisplayName("execute throws DomainException when first checker reports in-use")
    void execute_throwsDomainException_whenCheckerAReportsInUse() {
        when(repository.findById(2L)).thenReturn(Optional.of(stubGrid(2L)));
        when(checkerA.isUsed(2L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(2L));
        verify(repository, never()).delete(2L);
    }

    @Test
    @DisplayName("execute throws DomainException when second checker reports in-use")
    void execute_throwsDomainException_whenCheckerBReportsInUse() {
        when(repository.findById(3L)).thenReturn(Optional.of(stubGrid(3L)));
        when(checkerA.isUsed(3L)).thenReturn(false);
        when(checkerB.isUsed(3L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(3L));
        verify(repository, never()).delete(3L);
    }
}
