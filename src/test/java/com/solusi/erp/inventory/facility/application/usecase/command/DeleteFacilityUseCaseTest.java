package com.solusi.erp.inventory.facility.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.facility.domain.model.Facility;
import com.solusi.erp.inventory.facility.domain.repository.FacilityRepository;
import com.solusi.erp.inventory.facility.infrastructure.adapter.FacilityInUseCheckerComposite;
import com.solusi.erp.inventory.facility.domain.port.FacilityUsageChecker;
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

@DisplayName("DeleteFacilityUseCase Tests")
class DeleteFacilityUseCaseTest {

    private FacilityRepository repository;
    private FacilityUsageChecker checkerA;
    private FacilityUsageChecker checkerB;
    private DeleteFacilityUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(FacilityRepository.class);
        checkerA = mock(FacilityUsageChecker.class);
        checkerB = mock(FacilityUsageChecker.class);
        FacilityInUseCheckerComposite composite = new FacilityInUseCheckerComposite(List.of(checkerA, checkerB));
        useCase = new DeleteFacilityUseCaseImpl(repository, composite);
    }

    private Facility stubFacility(Long id) {
        AuditMetadata meta = new AuditMetadata(id, id, null, null, null, null);
        return new Facility(meta, "FAC-001", "Facility A", null, null, null, null, null, null, null, true);
    }

    @Test
    @DisplayName("execute deletes facility when no checker reports it as in-use")
    void execute_deletesFacility_whenNotInUse() {
        when(repository.findById(1L)).thenReturn(Optional.of(stubFacility(1L)));
        when(checkerA.isUsed(1L)).thenReturn(false);
        when(checkerB.isUsed(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when facility is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
        verify(repository, never()).delete(99L);
    }

    @Test
    @DisplayName("execute throws DomainException when first checker reports in-use")
    void execute_throwsDomainException_whenCheckerAReportsInUse() {
        when(repository.findById(2L)).thenReturn(Optional.of(stubFacility(2L)));
        when(checkerA.isUsed(2L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(2L));
        verify(repository, never()).delete(2L);
    }

    @Test
    @DisplayName("execute throws DomainException when second checker reports in-use")
    void execute_throwsDomainException_whenCheckerBReportsInUse() {
        when(repository.findById(3L)).thenReturn(Optional.of(stubFacility(3L)));
        when(checkerA.isUsed(3L)).thenReturn(false);
        when(checkerB.isUsed(3L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(3L));
        verify(repository, never()).delete(3L);
    }
}
