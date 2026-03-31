package com.solusi.erp.inventory.container.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.container.domain.model.Container;
import com.solusi.erp.inventory.container.domain.port.ContainerUsageChecker;
import com.solusi.erp.inventory.container.domain.repository.ContainerRepository;
import com.solusi.erp.inventory.container.infrastructure.adapter.ContainerInUseCheckerComposite;
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

@DisplayName("DeleteContainerUseCase Tests")
class DeleteContainerUseCaseTest {

    private ContainerRepository repository;
    private ContainerUsageChecker checkerA;
    private ContainerUsageChecker checkerB;
    private DeleteContainerUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ContainerRepository.class);
        checkerA = mock(ContainerUsageChecker.class);
        checkerB = mock(ContainerUsageChecker.class);
        ContainerInUseCheckerComposite composite = new ContainerInUseCheckerComposite(List.of(checkerA, checkerB));
        useCase = new DeleteContainerUseCaseImpl(repository, composite);
    }

    private Container stubContainer(Long id) {
        AuditMetadata meta = new AuditMetadata(id, id, null, null, null, null);
        return new Container(meta, 1L, null, null, "CONT-001", "Container A",
                null, null, null, null, null, null, true);
    }

    @Test
    @DisplayName("execute deletes container when no checker reports it as in-use")
    void execute_deletesContainer_whenNotInUse() {
        when(repository.findById(1L)).thenReturn(Optional.of(stubContainer(1L)));
        when(checkerA.isUsed(1L)).thenReturn(false);
        when(checkerB.isUsed(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when container is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
        verify(repository, never()).delete(99L);
    }

    @Test
    @DisplayName("execute throws DomainException when first checker reports in-use")
    void execute_throwsDomainException_whenCheckerAReportsInUse() {
        when(repository.findById(2L)).thenReturn(Optional.of(stubContainer(2L)));
        when(checkerA.isUsed(2L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(2L));
        verify(repository, never()).delete(2L);
    }

    @Test
    @DisplayName("execute throws DomainException when second checker reports in-use")
    void execute_throwsDomainException_whenCheckerBReportsInUse() {
        when(repository.findById(3L)).thenReturn(Optional.of(stubContainer(3L)));
        when(checkerA.isUsed(3L)).thenReturn(false);
        when(checkerB.isUsed(3L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(3L));
        verify(repository, never()).delete(3L);
    }
}
