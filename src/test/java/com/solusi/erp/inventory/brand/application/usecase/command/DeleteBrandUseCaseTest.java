package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.port.BrandInUseChecker;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("DeleteBrandUseCase Tests")
class DeleteBrandUseCaseTest {

    private BrandRepository repository;
    private BrandInUseChecker inUseChecker;
    private DeleteBrandUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        repository = mock(BrandRepository.class);
        inUseChecker = mock(BrandInUseChecker.class);
        useCase = new DeleteBrandUseCaseImpl(repository, inUseChecker);
    }

    @Test
    @DisplayName("execute deletes an existing brand when not in use")
    void execute_deletesExistingBrand() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Brand existing = new Brand(metadata, "BR-001", "Acme", "note");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isUsedByAnyProduct(1L)).thenReturn(false);

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when brand is not found")
    void execute_throwsDomainException_whenBrandNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
        verify(repository, never()).delete(99L);
    }

    @Test
    @DisplayName("execute throws DomainException when brand is in use by a product")
    void execute_throwsDomainException_whenInUse() {
        AuditMetadata metadata = new AuditMetadata(2L, 2L, null, null, null, null);
        Brand existing = new Brand(metadata, "BR-002", "Used Brand", "note");
        when(repository.findById(2L)).thenReturn(Optional.of(existing));
        when(inUseChecker.isUsedByAnyProduct(2L)).thenReturn(true);

        assertThrows(DomainException.class, () -> useCase.execute(2L));
        verify(repository, never()).delete(2L);
    }
}
