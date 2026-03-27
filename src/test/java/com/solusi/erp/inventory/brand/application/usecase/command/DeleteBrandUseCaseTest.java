package com.solusi.erp.inventory.brand.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.brand.domain.model.Brand;
import com.solusi.erp.inventory.brand.domain.repository.BrandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteBrandUseCase Tests")
class DeleteBrandUseCaseTest {

    @Mock
    private BrandRepository repository;

    private DeleteBrandUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteBrandUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute deletes an existing brand by id")
    void execute_deletesExistingBrand() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Brand existing = new Brand(metadata, "BR-001", "Acme", "note");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        useCase.execute(1L);

        verify(repository).delete(1L);
    }

    @Test
    @DisplayName("execute throws DomainException when brand is not found")
    void execute_throwsDomainException_whenBrandNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L));
    }
}
