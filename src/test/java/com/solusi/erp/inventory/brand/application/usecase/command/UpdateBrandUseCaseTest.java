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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateBrandUseCase Tests")
class UpdateBrandUseCaseTest {

    @Mock
    private BrandRepository repository;

    private UpdateBrandUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateBrandUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates name and note of an existing brand")
    void execute_updatesNameAndNoteOfExistingBrand() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Brand existing = new Brand(metadata, "BR-001", "Old Name", "Old Note");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Brand result = useCase.execute(1L, "New Name", "New Note");

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getNote()).isEqualTo("New Note");
        assertThat(result.getCode()).isEqualTo("BR-001");
    }

    @Test
    @DisplayName("execute throws DomainException when brand is not found")
    void execute_throwsDomainException_whenBrandNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class, () -> useCase.execute(99L, "Name", "Note"));
    }
}
